package club.ms.socios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "socios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_socio")
    private Long idSocio;

    @NotNull(message = "El tipo de documento es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_doc", nullable = false)
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El DNI es obligatorio")
    @Column(name = "dni", nullable = false)
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombres", nullable = false)
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "estado_membresia")
    private String estadoMembresia;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "clasificacion")
    private String clasificacion;

    @Email(message = "El correo debe ser válido")
    @Column(name = "correo")
    private String correo;
}
