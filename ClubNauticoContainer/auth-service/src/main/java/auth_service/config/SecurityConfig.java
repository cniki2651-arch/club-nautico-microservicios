package auth_service.config;

import auth_service.service.JwtService;
import jakarta.servlet.http.HttpServletResponse; // <-- nuevo import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder; // <-- nuevo import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                    System.out.println("[" + Thread.currentThread().getName() + "] === ACCESS DENIED ===");
                    System.out.println("[" + Thread.currentThread().getName() + "] Auth: " + auth);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    System.out.println("[" + Thread.currentThread().getName() + "] === AUTHENTICATION ENTRY POINT ===");
                    System.out.println("[" + Thread.currentThread().getName() + "] URI: " + request.getRequestURI() + " Metodo: " + request.getMethod());
                    System.out.println("[" + Thread.currentThread().getName() + "] Header Authorization: " + request.getHeader("Authorization"));
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                })
            )
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}