package auth_service.config;

import auth_service.entity.Permiso;
import auth_service.entity.Rol;
import auth_service.entity.RolPermiso;
import auth_service.repository.PermisoRepository;
import auth_service.repository.RolPermisoRepository;
import auth_service.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Siembra el catálogo de roles/permisos si la tabla está vacía. No toca ni
// depende de la autorización real (@PreAuthorize sigue usando ROLE_<n> del JWT
// tal cual) -- esto es solo el catálogo de referencia que pedía la profesora.
//
// IMPORTANTE: el orden de esta lista determina el id_rol autogenerado (IDENTITY),
// y debe coincidir exactamente con el role_id que ya usan los JWT y los
// @PreAuthorize("hasAuthority('ROLE_n')") en los 3 servicios: 1=Jefatura,
// 2=Secretaria, 3=Naviero, 4=Finanzas. No usar un Map aquí -- Map.of() no
// garantiza orden de iteración y desordenaría los IDs (bug ya encontrado).
@Component
@RequiredArgsConstructor
public class RolesPermisosSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;

    private static final List<String> ROLES_EN_ORDEN = List.of(
            "Jefatura",  // id_rol 1
            "Secretaria", // id_rol 2
            "Naviero",    // id_rol 3
            "Finanzas"    // id_rol 4
    );

    private static final List<String> PERMISOS = List.of(
            "GESTIONAR_USUARIOS",
            "GESTIONAR_SOCIOS",
            "GESTIONAR_NAUTICA",
            "GESTIONAR_FACTURACION",
            "APROBAR_SOLICITUDES"
    );

    @Override
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            ROLES_EN_ORDEN.forEach(nombre -> {
                Rol rol = new Rol();
                rol.setNombre(nombre);
                rolRepository.save(rol);
            });
        }

        if (permisoRepository.count() == 0) {
            PERMISOS.forEach(nombre -> {
                Permiso permiso = new Permiso();
                permiso.setNombre(nombre);
                permisoRepository.save(permiso);
            });
        }

        if (rolPermisoRepository.count() == 0) {
            Rol jefatura = rolRepository.findByNombre("Jefatura").orElse(null);
            if (jefatura != null) {
                permisoRepository.findAll().forEach(permiso -> {
                    RolPermiso rp = new RolPermiso();
                    rp.setRol(jefatura);
                    rp.setPermiso(permiso);
                    rolPermisoRepository.save(rp);
                });
            }
        }
    }
}
