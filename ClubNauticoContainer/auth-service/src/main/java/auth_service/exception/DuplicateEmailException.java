package auth_service.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String correo) {
        super("Ya existe una cuenta registrada con el correo: " + correo);
    }
}
