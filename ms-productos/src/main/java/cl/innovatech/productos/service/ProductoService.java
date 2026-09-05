package cl.innovatech.productos.service;

import cl.innovatech.productos.exception.RecursoNoEncontradoException;
import cl.innovatech.productos.model.Producto;
import cl.innovatech.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logica de negocio de productos.
 * Separa el controlador (capa web) del repositorio (capa de datos).
 */
@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el producto con id " + id));
    }

    public Producto crear(Producto producto) {
        producto.setId(null);
        return repository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto existente = obtener(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        return repository.save(existente);
    }

    public void eliminar(Long id) {
        Producto existente = obtener(id);
        repository.delete(existente);
    }
}
