package usco.co.asochazas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import usco.co.asochazas.model.Interaccion;
import usco.co.asochazas.security.CustomUserDetails;
import usco.co.asochazas.service.InteraccionService;

@RestController
@RequestMapping("/api/interacciones")
public class InteraccionController {

    @Autowired
    private InteraccionService interaccionService;

    public record ContactarRequest(Long productoId, int cantidad) {}

    @PostMapping
    public ResponseEntity<Interaccion> contactar(
            @AuthenticationPrincipal CustomUserDetails comprador,
            @RequestBody ContactarRequest req) {
        Interaccion creada = interaccionService.contactarVendedor(
                comprador.getId(), req.productoId(), req.cantidad());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Interaccion> confirmar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails vendedor) {
        return ResponseEntity.ok(interaccionService.confirmarVenta(id, vendedor.getId()));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Interaccion> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails vendedor) {
        return ResponseEntity.ok(interaccionService.marcarNoConcretada(id, vendedor.getId()));
    }

    @GetMapping("/recibidas")
    public ResponseEntity<List<Interaccion>> recibidos(@AuthenticationPrincipal CustomUserDetails vendedor) {
        return ResponseEntity.ok(interaccionService.listarContactosRecibidos(vendedor.getId()));
    }

    @GetMapping("/mis-contactos")
    public ResponseEntity<List<Interaccion>> misContactos(@AuthenticationPrincipal CustomUserDetails comprador) {
        return ResponseEntity.ok(interaccionService.listarMisContactos(comprador.getId()));
    }
}