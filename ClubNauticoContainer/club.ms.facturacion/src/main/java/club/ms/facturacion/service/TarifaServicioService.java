package club.ms.facturacion.service;

import club.ms.facturacion.dto.TarifaServicioResponse;
import club.ms.facturacion.repository.TarifaServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaServicioService {

    private final TarifaServicioRepository tarifaServicioRepository;

    public List<TarifaServicioResponse> listarActivas() {
        return tarifaServicioRepository.findByActivoTrueOrderByServicioAsc()
                .stream()
                .map(t -> new TarifaServicioResponse(t.getServicio(), t.getMonto()))
                .toList();
    }
}
