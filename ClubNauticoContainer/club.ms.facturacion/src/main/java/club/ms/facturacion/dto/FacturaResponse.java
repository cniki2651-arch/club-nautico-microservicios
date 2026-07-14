package club.ms.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaResponse {

    private Long idFactura;
    private Long idSocio;
    private String concepto;
    private BigDecimal montoBase;
    private BigDecimal montoTotal;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String estadoPago;
    private Integer idUsuarioEmisor;
    private Long idFacturaPadre;
    private Integer numeroCuota;
    private LocalDate fechaPago;
}
