package auth_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────
    // Estructura de respuesta de error estandarizada
    // ─────────────────────────────────────────────────────────────

    /**
     * Construye el cuerpo de error con la estructura estándar de producción:
     * { timestamp, status, error, message, path }
     */
    private Map<String, Object> buildError(HttpStatus status, String error,
                                           String message, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     error);
        body.put("message",   message);
        body.put("path",      request.getRequestURI());
        return body;
    }

    // ─────────────────────────────────────────────────────────────
    // 400 — Errores de validación de DTOs
    // ─────────────────────────────────────────────────────────────

    /**
     * Captura fallos de @NotBlank, @Email, @Size, etc.
     * Retorna el campo exacto y su mensaje de error.
     */
    /**
     * Correo ya registrado al intentar crear una cuenta nueva.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(
            DuplicateEmailException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
            buildError(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
            fieldErrors.put(fe.getField(), fe.getDefaultMessage())
        );

        Map<String, Object> body = buildError(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Uno o más campos no superaron la validación",
            request
        );
        body.put("errores", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — Autenticación fallida
    // ─────────────────────────────────────────────────────────────

    /**
     * Credenciales incorrectas (correo o contraseña inválidos).
     * Spring Security lanza BadCredentialsException en AuthenticationManager.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
            buildError(HttpStatus.UNAUTHORIZED, "Unauthorized",
                       "Correo o contraseña incorrectos", request),
            HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Token JWT expirado detectado en el filtro.
     */
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredToken(
            io.jsonwebtoken.ExpiredJwtException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
            buildError(HttpStatus.UNAUTHORIZED, "Token Expired",
                       "El token JWT ha expirado. Usa POST /auth/refresh para renovarlo.", request),
            HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Errores generales de autenticación y lógica de negocio
     * (usuario no encontrado, refresh token inválido, etc.)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
            buildError(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request),
            HttpStatus.UNAUTHORIZED
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — Autorización denegada
    // ─────────────────────────────────────────────────────────────

    /**
     * El usuario está autenticado pero no tiene el rol necesario.
     * Spring Security lanza AccessDeniedException vía @PreAuthorize.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
            buildError(HttpStatus.FORBIDDEN, "Forbidden",
                       "No tienes permisos para acceder a este recurso", request),
            HttpStatus.FORBIDDEN
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 423 — Cuenta bloqueada (fuerza bruta)
    // ─────────────────────────────────────────────────────────────

    /**
     * Cuenta bloqueada temporalmente por exceso de intentos fallidos.
     * Lanzado por LoginAttemptService → AuthService.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLocked(
            LockedException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
            buildError(HttpStatus.valueOf(423), "Account Locked", ex.getMessage(), request),
            HttpStatus.valueOf(423)
        );
    }

    /**
     * Otros estados de cuenta inválidos (deshabilitada, expirada, etc.)
     */
    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<Map<String, Object>> handleAccountStatus(
            AccountStatusException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
            buildError(HttpStatus.UNAUTHORIZED, "Account Status Error", ex.getMessage(), request),
            HttpStatus.UNAUTHORIZED
        );
    }
}