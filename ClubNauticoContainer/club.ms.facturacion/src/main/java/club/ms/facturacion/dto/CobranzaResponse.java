package club.ms.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CobranzaResponse {

    private Long idFactura;
    private Long idSocio;
    private String concepto;
    private BigDecimal montoBase;
    private BigDecimal interesesCalculados;
    private BigDecimal totalAcumulado;
    private LocalDate fechaVencimiento;
    private Long diasMora;
    private String estadoPago;
}
