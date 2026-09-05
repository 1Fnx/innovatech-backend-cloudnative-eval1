package cl.innovatech.pedidos.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Valida el claim "aud" (audience) del Access Token.
 *
 * Un token emitido para OTRA API no debe ser aceptado por este microservicio,
 * aunque su firma sea valida. Este validador implementa esa comprobacion.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final List<String> audienciasPermitidas;

    public AudienceValidator(List<String> audienciasPermitidas) {
        this.audienciasPermitidas = audienciasPermitidas;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audDelToken = jwt.getAudience();

        if (audDelToken != null) {
            for (String aud : audDelToken) {
                if (audienciasPermitidas.contains(aud)) {
                    return OAuth2TokenValidatorResult.success();
                }
            }
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "La audiencia (aud) del token no corresponde a esta API. Recibida: " + audDelToken,
                null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
