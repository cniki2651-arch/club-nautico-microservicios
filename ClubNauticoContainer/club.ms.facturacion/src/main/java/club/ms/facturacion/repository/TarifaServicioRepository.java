package club.ms.facturacion.repository;

import club.ms.facturacion.model.TarifaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarifaServicioRepository extends JpaRepository<TarifaServicio, Long> {
    List<TarifaServicio> findByActivoTrueOrderByServicioAsc();
}
