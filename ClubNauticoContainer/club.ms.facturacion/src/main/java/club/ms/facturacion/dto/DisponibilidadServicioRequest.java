package club.ms.facturacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadServicioRequest {

    @NotNull(message = "El campo disponible es obligatorio")
    private Boolean disponible;

    private String motivo;
}
