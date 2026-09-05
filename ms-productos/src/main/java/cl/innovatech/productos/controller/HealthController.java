package cl.innovatech.productos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint publico. Permite comprobar que el microservicio esta levantado
 * sin necesidad de presentar un token.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "servicio", "ms-productos",
                "mensaje", "Microservicio de Productos en ejecucion."
        ));
    }
}
