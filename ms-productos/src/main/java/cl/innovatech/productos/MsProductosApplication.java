package cl.innovatech.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Productos - Innovatech Chile
 * Asignatura DSY1107 - Desarrollo Cloud Native I
 *
 * Actua como OAuth2 Resource Server: valida los Access Token JWT
 * emitidos por Microsoft Entra External ID antes de exponer datos.
 */
@SpringBootApplication
public class MsProductosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProductosApplication.class, args);
    }
}
