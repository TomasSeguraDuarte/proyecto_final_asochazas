package usco.co.asochazas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.security.CustomUserDetails;
import usco.co.asochazas.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public record LoginRequest(String correo, String password) {}
    public record LoginResponse(String token, Long id, String nombre, String correo, String rol) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.correo().toLowerCase().trim(),
                            request.password()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            String token = jwtService.generarToken(userDetails);

            return new LoginResponse(
                    token,
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    usuario.getRol().getRol()
            );
        } catch (LockedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta se encuentra suspendida");
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
    }

    @GetMapping("/me")
    public Usuario perfilActual(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails.getUsuario();
    }
}