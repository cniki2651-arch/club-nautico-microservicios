package club.ms.socios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
