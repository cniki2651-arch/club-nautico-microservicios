package club.ms.socios.service;

import club.ms.socios.dto.TipoDocumentoResponse;
import club.ms.socios.exception.ResourceNotFoundException;
import club.ms.socios.model.TipoDocumento;
import club.ms.socios.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public List<TipoDocumentoResponse> listarTodos() {
        return tipoDocumentoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TipoDocumentoResponse buscarPorId(Integer id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de documento no encontrado con id: " + id));
        return toResponse(tipoDocumento);
    }

    private TipoDocumentoResponse toResponse(TipoDocumento entity) {
        return new TipoDocumentoResponse(
                entity.getIdTipoDoc(),
                entity.getSiglas(),
                entity.getDescripcion()
        );
    }
}
