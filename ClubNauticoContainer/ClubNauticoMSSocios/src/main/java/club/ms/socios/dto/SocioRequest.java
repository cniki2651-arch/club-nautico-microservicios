package club.ms.socios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SocioRequest {

    @NotNull(message = "El id del tipo de documento es obligatorio")
    private Integer idTipoDoc;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    private String telefono;

    private String estadoMembresia;

    private LocalDate fechaIngreso;

    private String clasificacion;

    @Email(message = "El correo debe ser válido")
    private String correo;
}
