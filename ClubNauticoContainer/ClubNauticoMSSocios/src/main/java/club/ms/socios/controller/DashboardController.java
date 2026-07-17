package club.ms.socios.controller;

import club.ms.socios.dto.MetricasSecretariaResponse;
import club.ms.socios.repository.SocioRepository;
import club.ms.socios.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SolicitudRepository solicitudRepository;
    private final SocioRepository socioRepository;

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @GetMapping("/secretaria")
    public ResponseEntity<MetricasSecretariaResponse> metricasSecretaria() {
        long solicitudesEnEspera = solicitudRepository.countByEstado("Pendiente");
        long sociosActivos = socioRepository.countByEstadoMembresiaNotIn(List.of("Pendiente", "Rechazado"));
        long alertas = socioRepository.countByEstadoMembresia("Moroso");
        return ResponseEntity.ok(new MetricasSecretariaResponse(solicitudesEnEspera, sociosActivos, alertas));
    }
}
