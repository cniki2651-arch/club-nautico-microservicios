package club.ms.socios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearRetiroRequest {

    @NotNull(message = "El id del socio es obligatorio")
    @JsonProperty("id_socio")
    private Long idSocio;

    private String motivo;
}
