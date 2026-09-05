package cl.innovatech.pedidos.controller;

import cl.innovatech.pedidos.model.Pedido;
import cl.innovatech.pedidos.repository.PedidoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints del microservicio de Pedidos, protegidos por scopes.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository repository;

    public PedidoController(PedidoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_Productos.Read')")
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_Productos.Read')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "No existe el pedido con id " + id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_Productos.Write')")
    public ResponseEntity<Pedido> crear(@Valid @RequestBody Pedido pedido) {
        pedido.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(pedido));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_Productos.Write')")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No existe el pedido con id " + id));
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Pedido eliminado correctamente."));
    }
}
