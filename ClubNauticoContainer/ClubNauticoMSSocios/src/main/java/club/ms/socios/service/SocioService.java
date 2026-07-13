package club.ms.socios.service;

import club.ms.socios.dto.SocioRequest;
import club.ms.socios.dto.SocioResponse;
import club.ms.socios.exception.ResourceNotFoundException;
import club.ms.socios.model.Socio;
import club.ms.socios.model.TipoDocumento;
import club.ms.socios.repository.SocioRepository;
import club.ms.socios.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocioService {

    private final SocioRepository socioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public List<SocioResponse> listarTodos() {
        return socioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SocioResponse buscarPorId(Long id) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Socio no encontrado con id: " + id));
        return toResponse(socio);
    }

    public SocioResponse crear(SocioRequest request) {
        Socio socio = toEntity(request, new Socio());
        Socio guardado = socioRepository.save(socio);
        return toResponse(guardado);
    }

    public SocioResponse actualizar(Long id, SocioRequest request) {
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Socio no encontrado con id: " + id));
        Socio actualizado = toEntity(request, socio);
        return toResponse(socioRepository.save(actualizado));
    }

    public void eliminar(Long id) {
        if (!socioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Socio no encontrado con id: " + id);
        }
        socioRepository.deleteById(id);
    }

    // --- Mappers privados ---

    private Socio toEntity(SocioRequest request, Socio socio) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.getIdTipoDoc())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de documento no encontrado con id: " + request.getIdTipoDoc()));

        socio.setTipoDocumento(tipoDocumento);
        socio.setDni(request.getDni());
        socio.setNombres(request.getNombres());
        socio.setApellidos(request.getApellidos());
        socio.setTelefono(request.getTelefono());
        socio.setEstadoMembresia(request.getEstadoMembresia());
        socio.setFechaIngreso(request.getFechaIngreso());
        socio.setClasificacion(request.getClasificacion());
        socio.setCorreo(request.getCorreo());
        return socio;
    }

    private SocioResponse toResponse(Socio socio) {
        return new SocioResponse(
                socio.getIdSocio(),
                socio.getTipoDocumento().getIdTipoDoc(),
                socio.getTipoDocumento().getSiglas(),
                socio.getDni(),
                socio.getNombres(),
                socio.getApellidos(),
                socio.getTelefono(),
                socio.getEstadoMembresia(),
                socio.getFechaIngreso(),
                socio.getClasificacion(),
                socio.getCorreo()
        );
    }
}
