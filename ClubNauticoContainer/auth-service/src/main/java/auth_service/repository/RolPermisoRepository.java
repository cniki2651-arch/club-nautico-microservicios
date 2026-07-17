package auth_service.repository;

import auth_service.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Integer> {
}
