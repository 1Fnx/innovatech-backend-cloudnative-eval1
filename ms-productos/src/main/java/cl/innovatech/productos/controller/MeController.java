package cl.innovatech.productos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Devuelve los claims del token recibido.
 * Sirve como evidencia de que el backend valida y lee correctamente el JWT.
 */
@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("autenticado", true);
        datos.put("sub", jwt.getSubject());
        datos.put("nombre", jwt.getClaimAsString("name"));
        datos.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null);
        datos.put("audience", jwt.getAudience());
        datos.put("scopes", jwt.getClaimAsString("scp"));
        datos.put("roles", jwt.getClaimAsStringList("roles"));
        datos.put("expira", jwt.getExpiresAt());
        return ResponseEntity.ok(datos);
    }
}
