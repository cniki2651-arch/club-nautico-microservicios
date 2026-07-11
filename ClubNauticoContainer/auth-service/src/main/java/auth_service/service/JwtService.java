package auth_service.service;

import auth_service.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets; // Importante para la clave fija

@Service
public class JwtService {
    
    // Clave estática de al menos 32 caracteres para HS256
    private final Key key = Keys.hmacShaKeyFor("EstaEsUnaClaveMuySecretaDeAlMenos32Caracteres!!!".getBytes(StandardCharsets.UTF_8));

    public String generateToken(Usuario usuario) {
        String roleString = usuario.getRol();
        Integer idRol = Integer.parseInt(roleString.replace("ROLE_", ""));
        
        return Jwts.builder()
                .setSubject(usuario.getCorreo())
                .claim("role_id", idRol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256) // Especificar algoritmo
                .compact();
    }

    public Integer extractRoleId(String token) {
    Object roleIdClaim = Jwts.parserBuilder()
            .setSigningKey(key)
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
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}