package club.ms.facturacion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consumo")
    private Long idConsumo;

    // Otro microservicio (ms-socios), solo se guarda el ID
    @NotNull(message = "El id del socio es obligatorio")
    @Column(name = "id_socio", nullable = false)
    private Long idSocio;

    @NotBlank(message = "El servicio es obligatorio")
    @Column(name = "servicio", nullable = false)
    private String servicio;

    @NotNull(message = "El monto es obligatorio")
    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "descripcion")
    private String descripcion;

    // Valores esperados: PENDIENTE, FACTURADO
    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "fecha_consumo", nullable = false)
    private LocalDateTime fechaConsumo;

    // Otro microservicio (Auth/IAM), solo se guarda el ID
    @Column(name = "id_usuario_registro")
    private Integer idUsuarioRegistro;

    // Un consumo puede o no estar ya agrupado dentro de una factura
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura")
    private Factura factura;
}
