package auth_service.repository;

import auth_service.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {
}
