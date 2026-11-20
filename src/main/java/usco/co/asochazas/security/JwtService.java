package usco.co.asochazas.security;

import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generarToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = Map.of(
                "userId", userDetails.getId(),
                "rol", userDetails.getUsuario().getRol().getRol(),
                "nombre", userDetails.getUsuario().getNombre()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public String extraerCorreo(String token) {
        return extraerTodosLosClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String correo = extraerCorreo(token);
        return correo.equals(userDetails.getUsername())
                && !estaExpirado(token)
                && userDetails.isAccountNonLocked();
    }

    private boolean estaExpirado(String token) {
        return extraerTodosLosClaims(token).getExpiration().before(new Date());
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
