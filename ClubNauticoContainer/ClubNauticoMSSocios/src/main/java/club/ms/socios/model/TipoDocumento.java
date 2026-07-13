package club.ms.socios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipos_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumento {

    @Id
    @Column(name = "id_tipo_doc")
    private Integer idTipoDoc;

    @Column(name = "siglas", nullable = false)
    private String siglas;

    @Column(name = "descripcion")
    private String descripcion;
}
