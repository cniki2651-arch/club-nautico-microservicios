package auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Maneja errores de autenticación o lógica de negocio (como usuario no encontrado)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", ex.getMessage());
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredToken(io.jsonwebtoken.ExpiredJwtException ex) {
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("error", "Token expirado, por favor inicia sesión de nuevo");
    errorDetails.put("status", 401);
    return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
}