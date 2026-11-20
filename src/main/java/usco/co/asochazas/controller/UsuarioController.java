package usco.co.asochazas.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.security.CustomUserDetails;
import usco.co.asochazas.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Usuario registrar(@RequestBody RegistroRequest request) {
        return usuarioService.createUsuario(request.nombre(), request.password(), request.correo());
    }

    @PutMapping("/perfil")
    public Usuario editarMiPerfil(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody EditarPerfilRequest request) {
        return usuarioService.editPerfil(user.getId(), request.nombre(), request.contacto());
    }

    @PutMapping("/{id}/suspender")
    public Usuario suspender(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestBody JustificacionRequest request) {
        return usuarioService.suspenderUsuario(id, admin.getId(), request.justificacion());
    }

    @PutMapping("/{id}/reactivar")
    public Usuario reactivar(@PathVariable Long id) {
        return usuarioService.reactivarUsuario(id);
    }

    @PutMapping("/{id}/revocar-vendedor")
    public Usuario revocarVendedor(
            @PathVariable Long id,
            @RequestBody JustificacionRequest request) {
        return usuarioService.revocarRolVendedor(id, request.justificacion());
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarUsuarios();
    }
    
    @PutMapping("/{id}/promover-admin")
    public Usuario promoverAdmin(@PathVariable Long id) {
        return usuarioService.promoverAdmin(id);
    }

    public record RegistroRequest(String nombre, String password, String correo) {}
    public record EditarPerfilRequest(String nombre, String contacto) {}
    public record JustificacionRequest(String justificacion) {}
}