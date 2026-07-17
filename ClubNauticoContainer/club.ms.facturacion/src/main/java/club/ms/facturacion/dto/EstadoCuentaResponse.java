package club.ms.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Nota: no incluye el nombre del socio (vive en ms-socios) -- el frontend lo
// cruza con GET /api/socios por id_socio.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoCuentaResponse {
    @JsonProperty("id_socio")
    private Long idSocio;
    @JsonProperty("total_deuda")
    private BigDecimal totalDeuda;
    private String estado;
}
