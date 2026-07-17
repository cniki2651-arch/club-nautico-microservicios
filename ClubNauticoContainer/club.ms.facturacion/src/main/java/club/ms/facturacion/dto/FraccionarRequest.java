package club.ms.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraccionarRequest {

    @NotNull(message = "El id de la factura es obligatorio")
    @JsonProperty("id_factura")
    private Long idFactura;

    @NotNull(message = "El numero de cuotas es obligatorio")
    @Min(value = 2, message = "Minimo 2 cuotas")
    @Max(value = 6, message = "Maximo 6 cuotas")
    private Integer cuotas;
}
