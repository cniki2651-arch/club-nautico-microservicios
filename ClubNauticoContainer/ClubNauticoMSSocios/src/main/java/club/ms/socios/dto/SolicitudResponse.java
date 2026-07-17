package club.ms.socios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponse {

    @JsonProperty("id_solicitud")
    private Long idSolicitud;

    @JsonProperty("tipo_solicitud")
    private String tipoSolicitud;

    private String estado;

    @JsonProperty("fecha_creacion")
    private LocalDateTime fechaCreacion;

    private String observacion;

    private String dni;

    private String nombres;

    private String apellidos;

    private String clasificacion;

    @JsonProperty("tipo_doc_siglas")
    private String tipoDocSiglas;
}
