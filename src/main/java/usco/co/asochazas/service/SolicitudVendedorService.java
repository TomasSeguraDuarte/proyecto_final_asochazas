package usco.co.asochazas.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import usco.co.asochazas.model.SolicitudVendedor;
import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.repository.EstadoSolicitudRepository;
import usco.co.asochazas.repository.RolRepository;
import usco.co.asochazas.repository.SolicitudVendedorRepository;
import usco.co.asochazas.repository.UsuarioRepository;

@Service
public class SolicitudVendedorService {
	
	@Autowired
	private RolRepository rolRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private SolicitudVendedorRepository solicitudRepository;
	
	@Autowired
	private EstadoSolicitudRepository estadoSolicitudRepository;
	
	public  SolicitudVendedor enviarSolicitud(Long usuarioId, String propuestaNombre, String propuestaDescripcion) {
		if(propuestaNombre.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere un nombre de propuesta");
		}
		if(propuestaDescripcion.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una descripcion valida");
		}
		Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
		
		if(usuario.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El ususario se encuentra suspendido");
		}
		
		if(usuario.getRol().getRol().equals("VENDEDOR")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El ususario ya posee el rol vendedor");
		}
	
		if(usuario.getRol().getRol().equals("ADMIN")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El ususario es un administrador");
		}
		
		if (solicitudRepository.existsByUsuarioIdAndEstado_EstadoSolicitud(usuarioId, "PENDIENTE")) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una solicitud pendiente para este usuario");
	    }
		
		SolicitudVendedor solicitud = new SolicitudVendedor();
		solicitud.setUsuario(usuario);
		solicitud.setPropuesta_nombre(propuestaNombre);
		solicitud.setPropuesta_descripcion(propuestaDescripcion);
		solicitud.setFecha_envio(LocalDateTime.now());
		solicitud.setEstado(estadoSolicitudRepository.findByEstadoSolicitud("PENDIENTE"));
	    return solicitudRepository.save(solicitud);
	}
	
	public Optional<SolicitudVendedor> consultarSolicitud(Long usuarioId) {
		if (!solicitudRepository.existsByUsuarioIdAndEstado_EstadoSolicitud(usuarioId, "PENDIENTE")) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "No existen solicitudes pendientes");
	    }
		
		return solicitudRepository.findByUsuarioId(usuarioId);
	}
	
	public SolicitudVendedor rechazarSolicitud(Long solicitudId,Long adminId,String justificacion) {
		SolicitudVendedor solicitud = solicitudRepository.findById(solicitudId).orElseThrow();
		Usuario admin = usuarioRepository.findById(adminId).orElseThrow();
		
		if(justificacion.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una justificacion valida");
		}
		if(!solicitud.getEstado().getEstadoSolicitud().equals("PENDIENTE")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta solicitud ya fue rechazada");
		}
		solicitud.setEstado(estadoSolicitudRepository.findByEstadoSolicitud("RECHAZADO"));
		solicitud.setJustificacion_admin(justificacion);
		solicitud.setFecha_registro(LocalDateTime.now());
		solicitud.setRevisado_por(admin);
		
		return solicitudRepository.save(solicitud);
	}
	
	public SolicitudVendedor aprobarSolicitud(Long solicitudId,Long adminId) {
		SolicitudVendedor solicitud = solicitudRepository.findById(solicitudId).orElseThrow();
		Usuario admin = usuarioRepository.findById(adminId).orElseThrow();
		
		if(!solicitud.getEstado().getEstadoSolicitud().equals("PENDIENTE")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta solicitud ya fue aprobada");
		}
		Usuario solicitante = solicitud.getUsuario(); 
	    solicitante.setRol(rolRepository.findByRol("VENDEDOR"));
	    usuarioRepository.save(solicitante);
		
		solicitud.setEstado(estadoSolicitudRepository.findByEstadoSolicitud("APROBADO"));
		solicitud.setFecha_registro(LocalDateTime.now());
		solicitud.setRevisado_por(admin);
		
		return solicitudRepository.save(solicitud);
	}
	
	public List<SolicitudVendedor> listarSolicitudesPendientes(){
		return solicitudRepository.findByEstado_EstadoSolicitud("PENDIENTE");
	}
		
}
