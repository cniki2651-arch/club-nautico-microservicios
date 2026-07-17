package club.ms.facturacion.service;

import club.ms.facturacion.dto.DisponibilidadServicioRequest;
import club.ms.facturacion.dto.DisponibilidadServicioResponse;
import club.ms.facturacion.model.DisponibilidadServicio;
import club.ms.facturacion.repository.DisponibilidadServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadServicioService {

    private final DisponibilidadServicioRepository disponibilidadServicioRepository;

    public List<DisponibilidadServicioResponse> listar() {
        return disponibilidadServicioRepository.findAll()
                .stream()
                .map(d -> new DisponibilidadServicioResponse(d.getServicio(), d.getDisponible(), d.getMotivo()))
                .toList();
    }

    public DisponibilidadServicioResponse actualizar(String servicio, DisponibilidadServicioRequest request) {
        DisponibilidadServicio disponibilidad = disponibilidadServicioRepository.findByServicio(servicio)
                .orElseGet(() -> {
                    DisponibilidadServicio nueva = new DisponibilidadServicio();
                    nueva.setServicio(servicio);
                    return nueva;
                });
        disponibilidad.setDisponible(request.getDisponible());
        disponibilidad.setMotivo(request.getMotivo());
        DisponibilidadServicio guardada = disponibilidadServicioRepository.save(disponibilidad);
        return new DisponibilidadServicioResponse(guardada.getServicio(), guardada.getDisponible(), guardada.getMotivo());
    }
}
