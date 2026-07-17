package club.ms.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadServicioResponse {
    private String servicio;
    private Boolean disponible;
    private String motivo;
}
