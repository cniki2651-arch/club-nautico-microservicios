package club.ms.socios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricasSecretariaResponse {
    private long solicitudesEnEspera;
    private long sociosActivos;
    private long alertas;
}
