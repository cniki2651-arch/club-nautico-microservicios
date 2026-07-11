package auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de validación de DTOs (@NotBlank, @Email, @Size, etc.)
     * Retorna un JSON con el nombre del campo y el mensaje de error específico.
     * Ejemplo de respuesta:
     * {
     *   "correo": "El formato del correo no es válido",
     *   "password": "La contraseña debe tener al menos 8 caracteres"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
            errores.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errores", errores);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de autenticación y lógica de negocio.
     * (Usuario no encontrado, contraseña incorrecta, refresh token expirado, etc.)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", ex.getMessage());
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Captura tokens JWT expirados que llegan al filtro.
     */
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredToken(io.jsonwebtoken.ExpiredJwtException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Token expirado. Usa el endpoint /auth/refresh para obtener un nuevo token.");
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
}