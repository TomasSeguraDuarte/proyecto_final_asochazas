package usco.co.asochazas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import usco.co.asochazas.model.SolicitudVendedor;
import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.repository.EstadoSolicitudRepository;
import usco.co.asochazas.repository.RolRepository;
import usco.co.asochazas.repository.SolicitudVendedorRepository;
import usco.co.asochazas.repository.UsuarioRepository;

@Service
public class SolicitudVendedorService {

    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SolicitudVendedorRepository solicitudRepository;
    @Autowired private EstadoSolicitudRepository estadoSolicitudRepository;

    public SolicitudVendedor enviarSolicitud(Long usuarioId, String propuestaNombre, String propuestaDescripcion) {
        if (propuestaNombre == null || propuestaNombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere un nombre de propuesta");
        }
        if (propuestaDescripcion == null || propuestaDescripcion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una descripcion valida");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario se encuentra suspendido");
        }
        if (usuario.getRol().getRol().equals("VENDEDOR")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya posee el rol vendedor");
        }
        if (usuario.getRol().getRol().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario es un administrador");
        }
        if (solicitudRepository.existsByUsuario_IdAndEstado_EstadoSolicitud(usuarioId, "PENDIENTE")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una solicitud pendiente para este usuario");
        }

        SolicitudVendedor solicitud = new SolicitudVendedor();
        solicitud.setUsuario(usuario);
        solicitud.setPropuesta_nombre(propuestaNombre.trim());
        solicitud.setPropuesta_descripcion(propuestaDescripcion.trim());
        solicitud.setFecha_envio(LocalDateTime.now());
        solicitud.setEstado(estadoSolicitudRepository.findByEstadoSolicitud("PENDIENTE"));
        return solicitudRepository.save(solicitud);
    }

    public SolicitudVendedor consultarSolicitud(Long usuarioId) {
        return solicitudRepository.findFirstByUsuario_IdOrderByIdDesc(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No tienes solicitudes registradas"));
    }

    public SolicitudVendedor rechazarSolicitud(Long solicitudId, Long adminId, String justificacion) {
        if (justificacion == null || justificacion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una justificacion valida");
        }
        SolicitudVendedor solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin no encontrado"));

        if (!solicitud.getEstado().getEstadoSolicitud().equals("PENDIENTE")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta solicitud ya fue procesada");
        }

        solicitud.setEstado(estadoSolicitudRepository.findByEstadoSolicitud("RECHAZADA"));
        solicitud.setJustificacion_admin(justificacion.trim());
        solicitud.setFecha_registro(LocalDateTime.now());
        solicitud.setRevisado_por(admin);
        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public SolicitudVendedor aprobarSolicitud(Long solicitudId, Long adminId) {
        SolicitudVendedor solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin no encontrado"));

        if (!solicitud.getEstado().getEstadoSolicitud().equals("PENDIENTE")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta solicitud ya fue procesada");
        }

        Usuario solicitante = solicitud.getUsuario();
        solicitante.setRol(rolRepository.findByRol("VENDEDOR"));
        usuarioRepository.save(solicitante);

        solicitud.setEstado(estadoSolicitudRepository.findByEstadoSolicitud("APROBADA"));
        solicitud.setFecha_registro(LocalDateTime.now());
        solicitud.setRevisado_por(admin);
        return solicitudRepository.save(solicitud);
    }

    public List<SolicitudVendedor> listarSolicitudesPendientes() {
        return solicitudRepository.findByEstado_EstadoSolicitud("PENDIENTE");
    }
}