package club.ms.socios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocioResponse {

    private Long idSocio;
    private Integer idTipoDoc;
    private String siglasTipoDoc;
    private String dni;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String estadoMembresia;
    private LocalDate fechaIngreso;
    private String clasificacion;
    private String correo;
}
