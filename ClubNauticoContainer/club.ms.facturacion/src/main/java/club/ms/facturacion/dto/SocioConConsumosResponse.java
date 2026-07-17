package club.ms.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Nota: ms-facturacion no tiene nombres/dni del socio (viven en ms-socios, otra
// base de datos) -- el frontend cruza esta respuesta con GET /api/socios por
// id_socio para mostrar el nombre.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocioConConsumosResponse {
    @JsonProperty("id_socio")
    private Long idSocio;
    @JsonProperty("total_consumos")
    private BigDecimal totalConsumos;
    private List<ConsumoDetalleResponse> consumos;
}
