package club.ms.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// El frontend (DashboardCobranza.tsx) lee estos 4 campos en snake_case para
// mostrar el desglose del pago en el toast de confirmación.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

    @JsonProperty("monto_base")
    private BigDecimal montoBase;

    @JsonProperty("interes_sbs")
    private BigDecimal interesSbs;

    @JsonProperty("dias_mora")
    private long diasMora;

    @JsonProperty("total_pagado")
    private BigDecimal totalPagado;
}
