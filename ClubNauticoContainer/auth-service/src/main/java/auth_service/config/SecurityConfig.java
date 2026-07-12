package auth_service.config;

import auth_service.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    System.out.println("[ACCESS DENIED] Auth: " + auth);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\": 401, \"error\": \"No autenticado. Incluye el header Authorization: Bearer <token>\"}"
                    );
                })
            )
            .build();
    }

    /**
     * Configuración de CORS para permitir peticiones desde frontend React (Vite o CRA).
     * Permite: localhost:3000 (Create React App) y localhost:5173 (Vite).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos (frontend React)
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173"
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos en la petición
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // Permite enviar cookies/credenciales (necesario si se usan cookies de refresh)
        config.setAllowCredentials(true);

        // Tiempo que el browser puede cachear la respuesta del preflight OPTIONS
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}