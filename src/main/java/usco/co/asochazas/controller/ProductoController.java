package usco.co.asochazas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import usco.co.asochazas.model.Producto;
import usco.co.asochazas.security.CustomUserDetails;
import usco.co.asochazas.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    public record CrearProductoRequest(int precio, int stock, String nombre, String descripcion, String categoria) {}
    public record EditarProductoRequest(Integer precio, Integer stock, String nombre, String descripcion, String categoria, String estado) {}
    public record JustificacionRequest(String justificacion) {}

    // Vendedor

    @PostMapping
    public ResponseEntity<Producto> crear(
            @AuthenticationPrincipal CustomUserDetails vendedor,
            @RequestBody CrearProductoRequest req) {
        Producto creado = productoService.crearProducto(
                vendedor.getId(), req.precio(), req.stock(), req.nombre(), req.descripcion(), req.categoria());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> editar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails vendedor,
            @RequestBody EditarProductoRequest req) {
        Producto actualizado = productoService.editarProducto(
                id, vendedor.getId(), req.precio(), req.stock(), req.nombre(), req.descripcion(), req.categoria(), req.estado());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Producto> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails vendedor) {
        return ResponseEntity.ok(productoService.eliminarProducto(id, vendedor.getId()));
    }

    @GetMapping("/mis-productos")
    public ResponseEntity<List<Producto>> listarMisProductos(@AuthenticationPrincipal CustomUserDetails vendedor) {
        return ResponseEntity.ok(productoService.listarMisProductos(vendedor.getId()));
    }

    // Publico

    @GetMapping
    public ResponseEntity<List<Producto>> listarCatalogo(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(productoService.listarCatalogo(categoria, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerDetalle(id));
    }

    // Admin

    @GetMapping("/admin/todos")
    public ResponseEntity<List<Producto>> listarTodosAdmin() {
        return ResponseEntity.ok(productoService.listarTodosAdmin());
    }

    @PutMapping("/{id}/ocultar")
    public ResponseEntity<Producto> ocultar(@PathVariable Long id, @RequestBody JustificacionRequest req) {
        return ResponseEntity.ok(productoService.ocultarProducto(id, req.justificacion()));
    }

    @PutMapping("/{id}/restaurar")
    public ResponseEntity<Producto> restaurar(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.restaurarProducto(id));
    }
}