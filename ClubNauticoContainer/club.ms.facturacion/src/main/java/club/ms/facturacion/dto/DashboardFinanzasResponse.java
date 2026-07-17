package club.ms.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFinanzasResponse {

    private Kpis kpis;
    private List<GraficaItem> graficaDistribucion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Kpis {
        private BigDecimal pendiente_facturar;
        private BigDecimal facturado_por_cobrar;
        private BigDecimal morosidad_total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraficaItem {
        private String nombre;
        private BigDecimal valor;
    }
}
