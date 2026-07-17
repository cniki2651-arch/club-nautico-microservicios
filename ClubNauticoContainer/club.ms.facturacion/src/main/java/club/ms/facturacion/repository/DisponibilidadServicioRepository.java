package club.ms.facturacion.repository;

import club.ms.facturacion.model.DisponibilidadServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisponibilidadServicioRepository extends JpaRepository<DisponibilidadServicio, Long> {
    Optional<DisponibilidadServicio> findByServicio(String servicio);
}
