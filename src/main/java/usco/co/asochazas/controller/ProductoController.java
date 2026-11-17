package usco.co.asochazas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import usco.co.asochazas.model.Producto;
import usco.co.asochazas.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // DTOs internos para Swagger / JSON
    public record CrearProductoRequest(
            Long vendedorId,
            int precio,
            int stock,
            String nombre,
            String descripcion,
            String categoria
    ) {}

    public record EditarProductoRequest(
            Long vendedorId,
            Integer precio,
            Integer stock,
            String nombre,
            String descripcion,
            String categoria,
            String estado
    ) {}

    // --- ENDPOINTS DE VENDEDOR ---

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody CrearProductoRequest req) {
        Producto creado = productoService.crearProducto(
                req.vendedorId(),
                req.precio(),
                req.stock(),
                req.nombre(),
                req.descripcion(),
                req.categoria()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> editar(@PathVariable Long id, @RequestBody EditarProductoRequest req) {
        Producto actualizado = productoService.editarProducto(
                id,
                req.vendedorId(),
                req.precio(),
                req.stock(),
                req.nombre(),
                req.descripcion(),
                req.categoria(),
                req.estado()
        );
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Producto> eliminar(@PathVariable Long id, @RequestParam Long vendedorId) {
        return ResponseEntity.ok(productoService.eliminarProducto(id, vendedorId));
    }

    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<List<Producto>> listarMisProductos(@PathVariable Long vendedorId) {
        return ResponseEntity.ok(productoService.listarMisProductos(vendedorId));
    }

    // --- ENDPOINTS DE CATÁLOGO PÚBLICO ---

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
}
