package usco.co.asochazas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import usco.co.asochazas.model.EstadoProducto;
import usco.co.asochazas.model.Producto;
import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.repository.EstadoProductoRepository;
import usco.co.asochazas.repository.EstadoUsuarioRepository;
import usco.co.asochazas.repository.ProductoRepository;
import usco.co.asochazas.repository.RolRepository;
import usco.co.asochazas.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private EstadoUsuarioRepository estadoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private EstadoProductoRepository estadoProductoRepository;

    public Usuario createUsuario(String nombre, String password, String correo) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre no puede estar vacio");
        }
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inserte una contraseña valida");
        }
        if (correo == null || !correo.toLowerCase().trim().endsWith("@usco.edu.co")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Debe utilizar un correo institucional");
        }
        String correoLimpio = correo.toLowerCase().trim();
        if (usuarioRepository.existsByCorreo(correoLimpio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Correo ya registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setFecha_registro(LocalDateTime.now());
        usuario.setCorreo(correoLimpio);
        usuario.setNombre(nombre.trim());
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEstado(estadoRepository.findByEstadoUsuario("ACTIVO"));
        usuario.setRol(rolRepository.findByRol("ESTUDIANTE"));
        return usuarioRepository.save(usuario);
    }

    public Usuario editPerfil(Long id, String nombre, String contacto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre no puede estar vacio");
        }
        if (usuario.getRol().getRol().equals("VENDEDOR") && (contacto == null || contacto.isBlank())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Como vendedor requiere una forma de contacto");
        }
        usuario.setNombre(nombre.trim());
        usuario.setContacto(contacto);
        return usuarioRepository.save(usuario);
    }

    public Usuario suspenderUsuario(Long id, Long adminId, String justificacion) {
        if (id.equals(adminId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes suspenderte a ti mismo");
        }
        if (justificacion == null || justificacion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una razon de suspension valida");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (usuario.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya se encuentra suspendido");
        }
        usuario.setEstado(estadoRepository.findByEstadoUsuario("SUSPENDIDO"));
        return usuarioRepository.save(usuario);
    }

    public Usuario reactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!usuario.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario no esta suspendido");
        }
        usuario.setEstado(estadoRepository.findByEstadoUsuario("ACTIVO"));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario revocarRolVendedor(Long id, String justificacion) {
        if (justificacion == null || justificacion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una justificacion para revocar el rol");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!usuario.getRol().getRol().equals("VENDEDOR")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario no tiene el rol VENDEDOR");
        }

        // Pausa todos sus productos activos para que no sigan visibles en el catálogo
        List<Producto> activos = productoRepository.findByVendedor_IdAndEstado_EstadoProducto(id, "ACTIVO");
        EstadoProducto pausado = estadoProductoRepository.findByEstadoProducto("PAUSADO");
        for (Producto p : activos) {
            p.setEstado(pausado);
        }
        productoRepository.saveAll(activos);

        usuario.setRol(rolRepository.findByRol("ESTUDIANTE"));
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
    
    @Transactional
    public Usuario promoverAdmin(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede promover a un usuario suspendido");
        }
        if (usuario.getRol().getRol().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya es administrador");
        }
        
        if (usuario.getRol().getRol().equals("VENDEDOR")) {
            List<Producto> activos = productoRepository.findByVendedor_IdAndEstado_EstadoProducto(id, "ACTIVO");
            EstadoProducto pausado = estadoProductoRepository.findByEstadoProducto("PAUSADO");
            for (Producto p : activos) {
                p.setEstado(pausado);
            }
            productoRepository.saveAll(activos);
        }

        usuario.setRol(rolRepository.findByRol("ADMIN"));
        return usuarioRepository.save(usuario);
    }
}
