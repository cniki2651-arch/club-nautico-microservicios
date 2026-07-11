package auth_service.config;

import auth_service.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // <- Asegúrate de añadir este import
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.core.context.SecurityContext;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService; // Se asigna correctamente[cite: 1]
    }

    @Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    logger.info("Header recibido: " + authHeader); // <-- nuevo diagnóstico

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            Integer roleId = jwtService.extractRoleId(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                String roleName = "ROLE_" + roleId;
                var authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

                logger.info("Auth establecida: " + SecurityContextHolder.getContext().getAuthentication()); // <-- de vuelta
            }
        } catch (Exception e) {
            logger.error("Error en JwtAuthenticationFilter: " + e.getMessage());
        }
    }
    filterChain.doFilter(request, response);
}
}