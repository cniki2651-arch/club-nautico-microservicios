package club.ms.socios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearSolicitudRequest {

    @NotNull(message = "El tipo de documento es obligatorio")
    @JsonProperty("id_tipo_doc")
    private Integer idTipoDoc;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos numéricos.")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    private String telefono;

    private String correo;

    @NotBlank(message = "La clasificación es obligatoria")
    private String clasificacion;

    @JsonProperty("tipo_solicitud")
    private String tipoSolicitud;
}
