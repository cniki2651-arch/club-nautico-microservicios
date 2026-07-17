package club.ms.socios.repository;

import club.ms.socios.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {
    Optional<Socio> findByDni(String dni);
    Optional<Socio> findByTipoDocumento_SiglasAndDni(String siglas, String dni);
    long countByEstadoMembresiaNotIn(java.util.List<String> estados);
    long countByEstadoMembresia(String estadoMembresia);
}
