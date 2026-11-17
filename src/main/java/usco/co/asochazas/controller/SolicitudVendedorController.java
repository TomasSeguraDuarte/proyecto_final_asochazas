package usco.co.asochazas.controller;

import java.util.List;

import java.util.Optional;

import org.springframework.web.bind.annotation.*;

import usco.co.asochazas.model.SolicitudVendedor;
import usco.co.asochazas.service.SolicitudVendedorService;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudVendedorController {

    private final SolicitudVendedorService solicitudService;

    public SolicitudVendedorController(SolicitudVendedorService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    public SolicitudVendedor enviar(@RequestBody EnviarSolicitudRequest request) {
        return solicitudService.enviarSolicitud(request.usuarioId(), request.propuestaNombre(), request.propuestaDescripcion());
    }

    @GetMapping("/usuario/{usuarioId}")
    public Optional<SolicitudVendedor> consultarMia(@PathVariable Long usuarioId) {
        return solicitudService.consultarSolicitud(usuarioId);
    }

    @GetMapping("/pendientes")
    public List<SolicitudVendedor> listarPendientes() {
        return solicitudService.listarSolicitudesPendientes();
    }

    @PutMapping("/{solicitudId}/aprobar")
    public SolicitudVendedor aprobar(@PathVariable Long solicitudId, @RequestBody AdminRequest request) {
        return solicitudService.aprobarSolicitud(solicitudId, request.adminId());
    }

    @PutMapping("/{solicitudId}/rechazar")
    public SolicitudVendedor rechazar(@PathVariable Long solicitudId, @RequestBody RechazarRequest request) {
        return solicitudService.rechazarSolicitud(solicitudId, request.adminId(), request.justificacion());
    }

    public record EnviarSolicitudRequest(Long usuarioId, String propuestaNombre, String propuestaDescripcion) {}
    public record AdminRequest(Long adminId) {}
    public record RechazarRequest(Long adminId, String justificacion) {}
}
