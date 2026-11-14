package usco.co.asochazas.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import usco.co.asochazas.model.Usuario;
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

    @PutMapping("/{id}")
    public Usuario editarPerfil(@PathVariable Long id, @RequestBody EditarPerfilRequest request) {
        return usuarioService.editPerfil(id, request.nombre(), request.contacto());
    }

    @PutMapping("/{id}/suspender")
    public Usuario suspender(@PathVariable Long id, @RequestBody SuspensionRequest request) {
        return usuarioService.suspenderUsuario(id, request.justificacion());
    }

    @PutMapping("/{id}/reactivar")
    public Usuario reactivar(@PathVariable Long id) {
        return usuarioService.reactivarUsuario(id);
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarUsuarios();
    }

    public record RegistroRequest(String nombre, String password, String correo) {}
    public record EditarPerfilRequest(String nombre, String contacto) {}
    public record SuspensionRequest(String justificacion) {}
}