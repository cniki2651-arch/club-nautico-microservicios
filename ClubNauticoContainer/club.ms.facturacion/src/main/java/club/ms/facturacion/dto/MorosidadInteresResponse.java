package club.ms.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MorosidadInteresResponse {

    private Long idMorosidad;
    private Long idFactura;
    private Integer diasRetraso;
    private BigDecimal montoInteresGenerado;
    private LocalDate fechaCalculo;
}
