package auth_service.repository;

import auth_service.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Cambia esto. Si antes tenías findByUsername, ahora debe ser findByCorreo
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}