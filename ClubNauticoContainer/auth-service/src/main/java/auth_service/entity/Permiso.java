package auth_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "permisos")
@Data
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_permiso;

    @Column(unique = true, nullable = false)
    private String nombre;

    private String descripcion;
}
