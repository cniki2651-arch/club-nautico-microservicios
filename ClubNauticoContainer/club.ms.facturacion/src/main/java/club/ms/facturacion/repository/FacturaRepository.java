package club.ms.facturacion.repository;

import club.ms.facturacion.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByEstadoPago(String estadoPago);

    List<Factura> findByIdSocio(Long idSocio);
}
