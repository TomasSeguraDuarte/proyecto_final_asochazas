package usco.co.asochazas.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import usco.co.asochazas.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            	.requestMatchers("/", "/*.html", "/css/**", "/js/**","/i18n/**", "/favicon.ico").permitAll()
                // Publico
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos").permitAll()

                // Funciones de administrador
                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/suspender", "/api/usuarios/*/reactivar", "/api/usuarios/*/revocar-vendedor","/api/usuarios/*/promover-admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/solicitudes/pendientes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/aprobar", "/api/solicitudes/*/rechazar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/productos/admin/todos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/productos/*/ocultar", "/api/productos/*/restaurar").hasRole("ADMIN")

                // Funciones de vendedor
                .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.PUT, "/api/productos/*").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/*").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.GET, "/api/productos/mis-productos").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.PUT, "/api/interacciones/*/confirmar", "/api/interacciones/*/cancelar").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.GET, "/api/interacciones/recibidas").hasRole("VENDEDOR")

                .requestMatchers(HttpMethod.GET, "/api/productos/{id}").permitAll()

                // Funciones de estudiante (hereda de vendedor)
                .requestMatchers(HttpMethod.POST, "/api/solicitudes").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.GET, "/api/solicitudes/mi-solicitud").hasAnyRole("ESTUDIANTE", "VENDEDOR")
                .requestMatchers(HttpMethod.POST, "/api/interacciones").hasAnyRole("ESTUDIANTE", "VENDEDOR")
                .requestMatchers(HttpMethod.GET, "/api/interacciones/mis-contactos").hasAnyRole("ESTUDIANTE", "VENDEDOR")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components().addSecuritySchemes("BearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}