package usco.co.asochazas.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import usco.co.asochazas.model.SolicitudVendedor;
import usco.co.asochazas.security.CustomUserDetails;
import usco.co.asochazas.service.SolicitudVendedorService;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudVendedorController {

    private final SolicitudVendedorService solicitudService;

    public SolicitudVendedorController(SolicitudVendedorService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    public SolicitudVendedor enviar(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody EnviarSolicitudRequest request) {
        return solicitudService.enviarSolicitud(user.getId(), request.propuestaNombre(), request.propuestaDescripcion());
    }

    @GetMapping("/mi-solicitud")
    public SolicitudVendedor consultarMia(@AuthenticationPrincipal CustomUserDetails user) {
        return solicitudService.consultarSolicitud(user.getId());
    }

    @GetMapping("/pendientes")
    public List<SolicitudVendedor> listarPendientes() {
        return solicitudService.listarSolicitudesPendientes();
    }

    @PutMapping("/{solicitudId}/aprobar")
    public SolicitudVendedor aprobar(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal CustomUserDetails admin) {
        return solicitudService.aprobarSolicitud(solicitudId, admin.getId());
    }

    @PutMapping("/{solicitudId}/rechazar")
    public SolicitudVendedor rechazar(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestBody RechazarRequest request) {
        return solicitudService.rechazarSolicitud(solicitudId, admin.getId(), request.justificacion());
    }

    public record EnviarSolicitudRequest(String propuestaNombre, String propuestaDescripcion) {}
    public record RechazarRequest(String justificacion) {}
}
