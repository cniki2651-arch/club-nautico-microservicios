package club.ms.socios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobarRetiroRequest {

    @NotNull(message = "El id de la solicitud es obligatorio")
    @JsonProperty("id_solicitud")
    private Long idSolicitud;

    @NotNull(message = "El id del socio es obligatorio")
    @JsonProperty("id_socio")
    private Long idSocio;
}
