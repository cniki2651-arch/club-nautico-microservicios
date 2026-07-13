package club.ms.socios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoResponse {

    private Integer idTipoDoc;
    private String siglas;
    private String descripcion;
}
