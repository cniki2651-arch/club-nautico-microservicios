package club.ms.socios.repository;

import club.ms.socios.model.Socio;
import club.ms.socios.model.SolicitudRetiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudRetiroRepository extends JpaRepository<SolicitudRetiro, Long> {
    List<SolicitudRetiro> findByEstadoSolicitudOrderByFechaSolicitudAsc(String estadoSolicitud);
    Optional<SolicitudRetiro> findFirstBySocioAndEstadoSolicitud(Socio socio, String estadoSolicitud);
}
