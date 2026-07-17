package club.ms.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumoDetalleResponse {
    @JsonProperty("id_consumo")
    private Long idConsumo;
    private String servicio;
    private BigDecimal monto;
    private String descripcion;
    @JsonProperty("fecha_consumo")
    private LocalDateTime fechaConsumo;
}
