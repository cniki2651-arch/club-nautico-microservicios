package auth_service.service;

import auth_service.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    // Clave y expiración leídas desde application.yaml
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un Access Token a partir del objeto Usuario completo.
     * Incluye el role_id en el payload.
     */
    public String generateToken(Usuario usuario) {
        String roleString = usuario.getRol();
        Integer idRol = Integer.parseInt(roleString.replace("ROLE_", ""));

        return Jwts.builder()
                .setSubject(usuario.getCorreo())
                .claim("role_id", idRol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un Access Token a partir de correo y roleId directamente.
     * Usado en el endpoint /auth/refresh para emitir nuevo token sin cargar al usuario.
     */
    public String generateTokenFromCorreo(String correo, Integer roleId) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("role_id", roleId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Integer extractRoleId(String token) {
        Object roleIdClaim = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role_id");

        if (roleIdClaim instanceof Number) {
            return ((Number) roleIdClaim).intValue();
        }
        return null;
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}