package club.ms.socios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluarSolicitudRequest {

    // El frontend manda snake_case aqui (estado_nuevo), a diferencia del resto
    // de DTOs del proyecto que usan camelCase.
    @NotBlank(message = "El estado nuevo es obligatorio")
    @JsonProperty("estado_nuevo")
    private String estadoNuevo;

    private String observacion;
}
