package cl.innovatech.productos.repository;

import cl.innovatech.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA. Spring Data genera automaticamente las operaciones
 * CRUD sobre la tabla "productos".
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
