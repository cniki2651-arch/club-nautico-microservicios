package club.ms.socios.repository;

import club.ms.socios.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findAllByOrderByFechaCreacionDesc();
    long countByEstado(String estado);
}
