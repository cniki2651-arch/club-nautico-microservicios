package club.ms.facturacion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    // No es @ManyToOne: id_socio pertenece a otro microservicio (ms-socios), solo guardamos el ID
    @NotNull(message = "El id del socio es obligatorio")
    @Column(name = "id_socio", nullable = false)
    private Long idSocio;

    @NotBlank(message = "El concepto es obligatorio")
    @Column(name = "concepto", nullable = false)
    private String concepto;

    @NotNull(message = "El monto base es obligatorio")
    @Column(name = "monto_base", nullable = false)
    private BigDecimal montoBase;

    @NotNull(message = "El monto total es obligatorio")
    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @NotNull(message = "La fecha de emision es obligatoria")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    // Valores esperados: VIGENTE, VENCIDO, PAGADO
    @Column(name = "estado_pago", nullable = false)
    private String estadoPago;

    // id del usuario (Auth/IAM) que emitio la factura, otro microservicio, solo se guarda el ID
    @Column(name = "id_usuario_emisor")
    private Integer idUsuarioEmisor;

    // Auto-referencia: si esta factura es una cuota de otra factura "padre" (fraccionamiento)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura_padre")
    private Factura facturaPadre;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;
}
