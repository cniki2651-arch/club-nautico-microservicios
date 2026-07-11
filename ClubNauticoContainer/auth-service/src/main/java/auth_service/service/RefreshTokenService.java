package auth_service.service;

import auth_service.entity.RefreshToken;
import auth_service.entity.Usuario;
import auth_service.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Gestiona el ciclo de vida completo de los Refresh Tokens:
 * creación, verificación de expiración y eliminación (logout / rotación).
 */
@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Genera un UUID único, lo persiste en BD asociado al usuario
     * con una fecha de expiración de N días.
     */
    @Transactional
    public RefreshToken createRefreshToken(Usuario usuario) {
        // Eliminar tokens anteriores del usuario (rotación: 1 refresh token activo por usuario)
        refreshTokenRepository.deleteByUsuario(usuario);

        RefreshToken refreshToken = new RefreshToken(
            UUID.randomUUID().toString(),
            usuario,
            Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS)
        );
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifica que el token no haya expirado.
     * Si expiró, lo elimina de la BD y lanza excepción.
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("El Refresh Token ha expirado. Por favor, inicia sesión de nuevo.");
        }
        return token;
    }

    /**
     * Elimina todos los refresh tokens de un usuario (usado en logout).
     */
    @Transactional
    public void deleteByUsuario(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);
    }
}
