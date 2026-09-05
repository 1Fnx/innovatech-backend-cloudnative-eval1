package cl.innovatech.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Pedidos - Innovatech Chile
 * Segundo microservicio de la solucion, protegido con el mismo IDaaS.
 */
@SpringBootApplication
public class MsPedidosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsPedidosApplication.class, args);
    }
}
