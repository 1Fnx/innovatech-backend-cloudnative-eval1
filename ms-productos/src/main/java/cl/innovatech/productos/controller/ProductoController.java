package cl.innovatech.productos.controller;

import cl.innovatech.productos.model.Producto;
import cl.innovatech.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints CRUD de productos, protegidos por scopes del Access Token.
 *
 * Lectura   -> requiere el scope Productos.Read
 * Escritura -> requiere el scope Productos.Write
 *
 * Si el token no contiene el scope exigido, Spring Security responde 403.
 * Si no hay token o es invalido, responde 401.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_Productos.Read')")
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_Productos.Read')")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_Productos.Write')")
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        Producto creado = service.crear(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_Productos.Write')")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id,
                                               @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(service.actualizar(id, producto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_Productos.Write')")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente."));
    }

    /**
     * Endpoint de ejemplo protegido por ROL (no por scope).
     * Demuestra la autorizacion por rol que exige la rubrica.
     */
    @GetMapping("/admin/resumen")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, Object>> resumenAdmin() {
        List<Producto> productos = service.listar();
        int stockTotal = productos.stream()
                .mapToInt(p -> p.getStock() == null ? 0 : p.getStock())
                .sum();
        return ResponseEntity.ok(Map.of(
                "totalProductos", productos.size(),
                "stockTotal", stockTotal,
                "mensaje", "Resumen visible solo para el rol Admin"
        ));
    }
}
