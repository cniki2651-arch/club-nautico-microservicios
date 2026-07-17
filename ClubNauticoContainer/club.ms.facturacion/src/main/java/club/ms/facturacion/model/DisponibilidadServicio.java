package club.ms.facturacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "disponibilidad_servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidad")
    private Long idDisponibilidad;

    @Column(name = "servicio", nullable = false, unique = true)
    private String servicio;

    @Column(name = "disponible", nullable = false)
    private Boolean disponible = true;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    @PreUpdate
    public void onSave() {
        actualizadoEn = LocalDateTime.now();
    }
}
