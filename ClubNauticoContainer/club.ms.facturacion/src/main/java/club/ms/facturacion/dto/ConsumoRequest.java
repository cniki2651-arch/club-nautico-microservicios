package club.ms.facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConsumoRequest {

    @NotNull(message = "El id del socio es obligatorio")
    private Long idSocio;

    @NotBlank(message = "El servicio es obligatorio")
    private String servicio;

    @NotNull(message = "El monto es obligatorio")
    private BigDecimal monto;

    private String descripcion;

    private String estado;

    private LocalDateTime fechaConsumo;

    private Integer idUsuarioRegistro;

    // Id de la factura a la que se asocia este consumo (opcional, puede quedar sin facturar aun)
    private Long idFactura;
}
