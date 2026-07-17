package club.ms.facturacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tarifas_servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Long idTarifa;

    @Column(name = "servicio", nullable = false)
    private String servicio;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
