package club.ms.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumoResponse {

    private Long idConsumo;
    private Long idSocio;
    private String servicio;
    private BigDecimal monto;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaConsumo;
    private Integer idUsuarioRegistro;
    private Long idFactura;
}
