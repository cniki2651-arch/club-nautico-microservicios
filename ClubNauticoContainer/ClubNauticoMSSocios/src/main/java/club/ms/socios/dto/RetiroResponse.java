package club.ms.socios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// El frontend (TablaRetirosPendientes.tsx) espera exactamente estos nombres en
// snake_case -- viene de cuando el backend original armaba el JSON a mano desde
// filas de Postgres.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetiroResponse {

    @JsonProperty("id_solicitud")
    private Long idSolicitud;

    @JsonProperty("id_socio")
    private Long idSocio;

    private String motivo;

    @JsonProperty("fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @JsonProperty("estado_solicitud")
    private String estadoSolicitud;

    private String nombres;

    private String apellidos;

    private String dni;

    @JsonProperty("deuda_pendiente")
    private BigDecimal deudaPendiente;
}
