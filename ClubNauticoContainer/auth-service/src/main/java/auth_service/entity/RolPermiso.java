package auth_service.entity;

import jakarta.persistence.*;
import lombok.Data;

// Tabla puente rol <-> permiso. Ojo: por ahora es solo un catálogo de referencia,
// no está conectada a las anotaciones @PreAuthorize que ya funcionan en los 3
// servicios Java (esas siguen usando el rol numérico del JWT tal cual). Cablear
// permisos granulares de verdad implicaría tocar la seguridad de los 3 servicios
// y no se hizo por el tiempo disponible.
@Entity
@Table(name = "roles_permisos")
@Data
public class RolPermiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_rol_permiso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}
