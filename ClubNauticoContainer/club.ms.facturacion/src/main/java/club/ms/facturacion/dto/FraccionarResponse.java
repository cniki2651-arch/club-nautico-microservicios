package club.ms.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraccionarResponse {
    private String mensaje;
    @JsonProperty("cuotas_generadas")
    private Integer cuotasGeneradas;
    @JsonProperty("monto_por_cuota")
    private BigDecimal montoPorCuota;
}
