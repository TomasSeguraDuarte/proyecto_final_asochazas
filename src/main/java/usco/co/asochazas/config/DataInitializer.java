package usco.co.asochazas.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import usco.co.asochazas.model.Categoria;
import usco.co.asochazas.model.EstadoInteraccion;
import usco.co.asochazas.model.EstadoProducto;
import usco.co.asochazas.model.EstadoSolicitud;
import usco.co.asochazas.model.EstadoUsuario;
import usco.co.asochazas.model.Rol;
import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.repository.CategoriaRepository;
import usco.co.asochazas.repository.EstadoInteraccionRepository;
import usco.co.asochazas.repository.EstadoProductoRepository;
import usco.co.asochazas.repository.EstadoSolicitudRepository;
import usco.co.asochazas.repository.EstadoUsuarioRepository;
import usco.co.asochazas.repository.RolRepository;
import usco.co.asochazas.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired 
    private RolRepository rolRepository;
    @Autowired 
    private EstadoUsuarioRepository estadoUsuarioRepository;
    @Autowired 
    private EstadoSolicitudRepository estadoSolicitudRepository;
    @Autowired 
    private EstadoProductoRepository estadoProductoRepository;
    @Autowired 
    private EstadoInteraccionRepository estadoInteraccionRepository;
    @Autowired 
    private CategoriaRepository categoriaRepository;
    @Autowired 
    private UsuarioRepository usuarioRepository;
    @Autowired 
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Roles del sistema
        for (String nombre : List.of("ESTUDIANTE", "VENDEDOR", "ADMIN")) {
            if (rolRepository.findByRol(nombre) == null) {
                Rol r = new Rol();
                r.setRol(nombre);
                rolRepository.save(r);
            }
        }

        // Estados de usuario
        for (String estado : List.of("ACTIVO", "SUSPENDIDO", "PENDIENTE")) {
            if (estadoUsuarioRepository.findByEstadoUsuario(estado) == null) {
                EstadoUsuario eu = new EstadoUsuario();
                eu.setEstadoUsuario(estado);
                estadoUsuarioRepository.save(eu);
            }
        }

        // Estados de solicitud de vendedor (femenino, tal como los valida SolicitudVendedorService)
        for (String estado : List.of("PENDIENTE", "APROBADA", "RECHAZADA", "CAMBIOS_SOLICITADOS")) {
            if (estadoSolicitudRepository.findByEstadoSolicitud(estado) == null) {
                EstadoSolicitud es = new EstadoSolicitud();
                es.setEstadoSolicitud(estado);
                estadoSolicitudRepository.save(es);
            }
        }

        // Estados de producto
        for (String estado : List.of("ACTIVO", "PAUSADO", "OCULTO", "ELIMINADO")) {
            if (estadoProductoRepository.findByEstadoProducto(estado) == null) {
                EstadoProducto ep = new EstadoProducto();
                ep.setEstadoProducto(estado);
                estadoProductoRepository.save(ep);
            }
        }

        // Estados de interacción
        for (String estado : List.of("CONTACTADO", "VENTA_CONFIRMADA", "NO_CONCRETADA")) {
            if (estadoInteraccionRepository.findByEstadoInteraccion(estado) == null) {
                EstadoInteraccion ei = new EstadoInteraccion();
                ei.setEstadoInteraccion(estado);
                estadoInteraccionRepository.save(ei);
            }
        }

        // Categorías de productos
        for (String nombreCat : List.of(
                "Comida",
                "Tecnología",
                "Ropa",
                "Accesorios",
                "Papelería",
                "Otros"
        )) {
            if (categoriaRepository.findByCategoria(nombreCat) == null) {
                Categoria cat = new Categoria();
                cat.setCategoria(nombreCat);
                categoriaRepository.save(cat);
            }
        }

        String correoAdmin = "admin@usco.edu.co";
        if (!usuarioRepository.existsByCorreo(correoAdmin)) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador USCO");
            admin.setCorreo(correoAdmin);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFecha_registro(LocalDateTime.now());
            admin.setRol(rolRepository.findByRol("ADMIN"));
            admin.setEstado(estadoUsuarioRepository.findByEstadoUsuario("ACTIVO"));
            usuarioRepository.save(admin);
        }
    }
}
