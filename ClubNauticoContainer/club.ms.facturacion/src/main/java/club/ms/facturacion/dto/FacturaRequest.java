package club.ms.facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FacturaRequest {

    @NotNull(message = "El id del socio es obligatorio")
    private Long idSocio;

    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    @NotNull(message = "El monto base es obligatorio")
    private BigDecimal montoBase;

    @NotNull(message = "El monto total es obligatorio")
    private BigDecimal montoTotal;

    @NotNull(message = "La fecha de emision es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    private String estadoPago;

    private Integer idUsuarioEmisor;

    // Id de la factura padre, si esta es una cuota de un fraccionamiento (opcional)
    private Long idFacturaPadre;

    private Integer numeroCuota;

    private LocalDate fechaPago;
}
