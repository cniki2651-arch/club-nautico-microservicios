package club.ms.facturacion.repository;

import club.ms.facturacion.model.MorosidadInteres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MorosidadInteresRepository extends JpaRepository<MorosidadInteres, Long> {

    List<MorosidadInteres> findByFacturaIdFactura(Long idFactura);

    // Ultimo calculo registrado para una factura, ordenado por fecha descendente
    Optional<MorosidadInteres> findFirstByFacturaIdFacturaOrderByFechaCalculoDesc(Long idFactura);
}
