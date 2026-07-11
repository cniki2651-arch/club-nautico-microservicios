package auth_service.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protección contra fuerza bruta mediante caché en memoria (thread-safe).
 *
 * Lógica:
 *  - Tras MAX_ATTEMPTS fallos consecutivos, la cuenta queda bloqueada BLOCK_DURATION_MINUTES minutos.
 *  - Un login exitoso resetea el contador.
 *  - No persiste entre reinicios del servidor (tradeoff aceptable sin Redis).
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS         = 5;
    private static final long BLOCK_DURATION_MINUTES = 15L;

    /**
     * Registro de intentos por correo electrónico.
     * Clave: correo | Valor: AttemptData (contador + timestamp del primer fallo)
     */
    private final ConcurrentHashMap<String, AttemptData> attemptsCache = new ConcurrentHashMap<>();

    // ─────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────

    /**
     * Llamar cuando el login falla. Incrementa el contador del correo.
     */
    public void loginFailed(String correo) {
        attemptsCache.merge(correo, new AttemptData(1, Instant.now()),
            (existing, newEntry) -> {
                existing.count++;
                return existing;
            });
    }

    /**
     * Llamar cuando el login es exitoso. Elimina el registro de intentos.
     */
    public void loginSucceeded(String correo) {
        attemptsCache.remove(correo);
    }

    /**
     * Retorna true si la cuenta está bloqueada por exceso de intentos fallidos.
     * Si el bloqueo ya expiró, limpia automáticamente el registro.
     */
    public boolean isBlocked(String correo) {
        AttemptData data = attemptsCache.get(correo);
        if (data == null) return false;

        // Si el tiempo de bloqueo ha expirado, limpiar y permitir
        long minutosTranscurridos = java.time.Duration
            .between(data.firstAttemptTime, Instant.now())
            .toMinutes();

        if (minutosTranscurridos >= BLOCK_DURATION_MINUTES) {
            attemptsCache.remove(correo);
            return false;
        }

        return data.count >= MAX_ATTEMPTS;
    }

    /**
     * Retorna los minutos restantes de bloqueo (útil para mensajes de error).
     */
    public long getRemainingBlockMinutes(String correo) {
        AttemptData data = attemptsCache.get(correo);
        if (data == null) return 0;

        long minutosTranscurridos = java.time.Duration
            .between(data.firstAttemptTime, Instant.now())
            .toMinutes();

        long remaining = BLOCK_DURATION_MINUTES - minutosTranscurridos;
        return Math.max(0, remaining);
    }

    // ─────────────────────────────────────────────────────────────
    // Clase interna de datos
    // ─────────────────────────────────────────────────────────────

    private static class AttemptData {
        int count;
        final Instant firstAttemptTime;

        AttemptData(int count, Instant firstAttemptTime) {
            this.count = count;
            this.firstAttemptTime = firstAttemptTime;
        }
    }
}
