package club.ms.facturacion.repository;

import club.ms.facturacion.model.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    List<Consumo> findByIdSocio(Long idSocio);

    List<Consumo> findByFacturaIsNull();
}
