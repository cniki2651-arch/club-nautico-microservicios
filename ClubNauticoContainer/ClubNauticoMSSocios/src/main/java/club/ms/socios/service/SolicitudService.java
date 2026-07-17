package club.ms.socios.service;

import club.ms.socios.dto.CrearSolicitudRequest;
import club.ms.socios.dto.EvaluarSolicitudRequest;
import club.ms.socios.dto.SolicitudResponse;
import club.ms.socios.exception.BusinessRuleException;
import club.ms.socios.exception.ResourceNotFoundException;
import club.ms.socios.model.Socio;
import club.ms.socios.model.Solicitud;
import club.ms.socios.model.TipoDocumento;
import club.ms.socios.repository.SocioRepository;
import club.ms.socios.repository.SolicitudRepository;
import club.ms.socios.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final SocioRepository socioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public List<SolicitudResponse> listar() {
        return solicitudRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SolicitudResponse crear(CrearSolicitudRequest request) {
        // Regla de negocio: el club solo acepta socios con clasificación "Pagador"
        String clasificacion = request.getClasificacion() == null ? "" : request.getClasificacion().trim().toLowerCase();
        if (!clasificacion.contains("pagador")) {
            throw new BusinessRuleException(
                    "Rechazado por política de riesgo: el club solo acepta socios con clasificación de tipo Pagador.");
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.getIdTipoDoc())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de documento no encontrado con id: " + request.getIdTipoDoc()));

        Socio socio = socioRepository.findByDni(request.getDni()).orElse(null);

        if (socio != null) {
            if ("Rechazado".equalsIgnoreCase(socio.getEstadoMembresia())) {
                // Se permite subsanar: reintenta la inscripción sobre el mismo registro
                socio.setTipoDocumento(tipoDocumento);
                socio.setNombres(request.getNombres());
                socio.setApellidos(request.getApellidos());
                socio.setTelefono(request.getTelefono());
                socio.setCorreo(request.getCorreo());
                socio.setClasificacion(request.getClasificacion());
                socio.setEstadoMembresia("Pendiente");
            } else {
                throw new BusinessRuleException(
                        "Rechazado: el número de documento ingresado ya tiene una inscripción activa o pendiente registrada en el Club.");
            }
        } else {
            socio = new Socio();
            socio.setTipoDocumento(tipoDocumento);
            socio.setDni(request.getDni());
            socio.setNombres(request.getNombres());
            socio.setApellidos(request.getApellidos());
            socio.setTelefono(request.getTelefono());
            socio.setCorreo(request.getCorreo());
            socio.setClasificacion(request.getClasificacion());
            socio.setEstadoMembresia("Pendiente");
        }
        Socio socioGuardado = socioRepository.save(socio);

        Solicitud solicitud = new Solicitud();
        solicitud.setSocio(socioGuardado);
        solicitud.setTipoSolicitud(request.getTipoSolicitud() != null ? request.getTipoSolicitud() : "Inscripción");
        solicitud.setEstado("Pendiente");
        Solicitud guardada = solicitudRepository.save(solicitud);

        return toResponse(guardada);
    }

    @Transactional
    public void evaluar(Long id, EvaluarSolicitudRequest request, String usuarioRevisor) {
        String estadoNuevo = request.getEstadoNuevo();
        if (!"Aprobado".equals(estadoNuevo) && !"Rechazado".equals(estadoNuevo)) {
            throw new BusinessRuleException("El estado debe ser Aprobado o Rechazado.");
        }

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id: " + id));

        solicitud.setEstado(estadoNuevo);
        solicitud.setObservacion(request.getObservacion());
        solicitud.setUsuarioRevisor(usuarioRevisor);
        solicitud.setFechaResolucion(java.time.LocalDateTime.now());
        solicitudRepository.save(solicitud);

        Socio socio = solicitud.getSocio();
        socio.setEstadoMembresia("Aprobado".equals(estadoNuevo) ? "Al día" : "Rechazado");
        socioRepository.save(socio);
    }

    private SolicitudResponse toResponse(Solicitud s) {
        Socio socio = s.getSocio();
        return new SolicitudResponse(
                s.getIdSolicitud(),
                s.getTipoSolicitud(),
                s.getEstado(),
                s.getFechaCreacion(),
                s.getObservacion(),
                socio.getDni(),
                socio.getNombres(),
                socio.getApellidos(),
                socio.getClasificacion(),
                socio.getTipoDocumento() != null ? socio.getTipoDocumento().getSiglas() : null
        );
    }
}
