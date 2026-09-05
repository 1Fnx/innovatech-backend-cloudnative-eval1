package cl.innovatech.productos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Configuracion de seguridad del microservicio.
 *
 * Convierte la API en un OAuth2 Resource Server que valida los Access Token
 * JWT emitidos por Microsoft Entra External ID. Se comprueban:
 *
 *   - FIRMA      : mediante las claves publicas del IdP (endpoint JWKS)
 *   - ISSUER     : que el token venga del tenant correcto
 *   - AUDIENCE   : que el token sea para ESTA API (AudienceValidator)
 *   - EXPIRACION : validada por defecto por JwtValidators
 *   - SCOPES/ROLES: autorizacion por permiso en cada endpoint
 *
 * Codigos de respuesta:
 *   401 Unauthorized -> no hay token, o el token es invalido/expirado
 *   403 Forbidden    -> token valido pero sin el permiso requerido
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.security.issuer-uri}")
    private String issuerUri;

    @Value("${app.security.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.security.audiences}")
    private String audiences;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF se deshabilita: la API es stateless y usa Bearer Tokens
            .csrf(csrf -> csrf.disable())

            // CORS: permite que el frontend (otro origen) consuma la API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Sin sesiones: cada peticion se autentica con su propio token
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Preflight CORS del navegador
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Endpoint publico de salud (permite comprobar que el servicio esta arriba)
                .requestMatchers("/api/health").permitAll()
                // Consola H2 solo en modo demo
                .requestMatchers("/h2-console/**").permitAll()
                // Todo lo demas exige un token valido
                .anyRequest().authenticated()
            )

            // Configura el backend como Resource Server que procesa JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )

            // Permite que la consola H2 se muestre en un frame (solo demo)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * Decodificador de JWT.
     * Descarga las claves publicas desde el JWKS del tenant para verificar la
     * firma, y encadena los validadores de issuer, expiracion y audiencia.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();

        // Validador por defecto: comprueba expiracion (exp) e issuer (iss)
        OAuth2TokenValidator<Jwt> conIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);

        // Validador propio: comprueba la audiencia (aud)
        List<String> listaAudiencias = Arrays.stream(audiences.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        OAuth2TokenValidator<Jwt> conAudiencia = new AudienceValidator(listaAudiencias);

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(conIssuer, conAudiencia));
        return decoder;
    }

    /**
     * Convierte los claims del token en autoridades de Spring Security:
     *   - claim "scp"   -> autoridades con prefijo SCOPE_  (ej: SCOPE_Productos.Read)
     *   - claim "roles" -> autoridades con prefijo ROLE_   (ej: ROLE_Admin)
     *
     * Esto permite usar @PreAuthorize("hasAuthority('SCOPE_Productos.Read')")
     * y hasRole('Admin') en los controladores.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Scopes -> SCOPE_xxx (Entra ID entrega los scopes en el claim "scp")
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setAuthorityPrefix("SCOPE_");
        scopesConverter.setAuthoritiesClaimName("scp");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<org.springframework.security.core.GrantedAuthority> authorities =
                    new ArrayList<>(scopesConverter.convert(jwt));

            // Roles de aplicacion -> ROLE_xxx
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(rol -> authorities.add(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol)));
            }
            return authorities;
        });
        return converter;
    }

    /**
     * Politica CORS: solo se aceptan peticiones desde los origenes declarados.
     * No se usa "*" para evitar sobrepermisos.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
