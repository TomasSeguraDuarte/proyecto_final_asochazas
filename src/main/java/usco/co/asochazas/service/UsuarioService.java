package usco.co.asochazas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import usco.co.asochazas.model.Rol;
import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.repository.EstadoUsuarioRepository;
import usco.co.asochazas.repository.RolRepository;
import usco.co.asochazas.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private RolRepository rolRepository;
	
	@Autowired
	private EstadoUsuarioRepository estadoRepository;
	
	
	public Usuario createUsuario(String nombre, String password, String correo) {
		Usuario usuario = new Usuario();
		if(nombre.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario no puede estar vacio");
		}
		if(password.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Inserte una contraseña valida");
		}
		if (!correo.toLowerCase().endsWith("@usco.edu.co")) {
		    throw new ResponseStatusException(HttpStatus.CONFLICT, "Debe utilizar un correo institucional");
		}
		if(usuarioRepository.existsByCorreo(correo)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Correo ya registrado");
		}
		usuario.setCorreo(correo);
		usuario.setNombre(nombre);
		usuario.setPassword(passwordEncoder.encode(password));
		usuario.setEstado(estadoRepository.findByEstadoUsuario("ACTIVO"));
		usuario.setRol(rolRepository.findByRol("ESTUDIANTE")); //todo, primero se deben sembrar roles
		return usuarioRepository.save(usuario);
	}
	
	public Usuario editPerfil(Long id, String nombre, String contacto) {
		Usuario usuario = usuarioRepository.findById(id).orElseThrow();
		if(nombre.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario no puede estar vacio");
		}
		if (usuario.getRol().getRol().equals("VENDEDOR") && contacto.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Como vendedor requiere una forma de contacto");
		}
		
		usuario.setNombre(nombre);
		usuario.setContacto(contacto);
		
		return usuarioRepository.save(usuario);
	}
	
	public Usuario suspenderUsuario(Long id, String justificacion) {
		Usuario usuario = usuarioRepository.findById(id).orElseThrow();
		if(justificacion.isBlank())
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una razon de suspension valida");
		
		if (usuario.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya se encuentra suspendido");
		}
		
		usuario.setEstado(estadoRepository.findByEstadoUsuario("SUSPENDIDO"));
		return usuarioRepository.save(usuario);
	}
	
	public Usuario reactivarUsuario(Long id) {
		Usuario usuario = usuarioRepository.findById(id).orElseThrow();
		if (!usuario.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario no esta suspendido");
		}
		usuario.setEstado(estadoRepository.findByEstadoUsuario("ACTIVO"));
		return usuarioRepository.save(usuario);
	}
	
	public List<Usuario> listarUsuarios(){
		return usuarioRepository.findAll();
	}
	
	
}
