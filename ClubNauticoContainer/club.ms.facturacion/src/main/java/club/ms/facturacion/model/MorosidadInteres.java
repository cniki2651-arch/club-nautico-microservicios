package club.ms.facturacion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "morosidad_intereses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MorosidadInteres {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_morosidad")
    private Long idMorosidad;

    @NotNull(message = "La factura es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;

    @Column(name = "dias_retraso", nullable = false)
    private Integer diasRetraso;

    @Column(name = "monto_interes_generado", nullable = false)
    private BigDecimal montoInteresGenerado;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDate fechaCalculo;
}
