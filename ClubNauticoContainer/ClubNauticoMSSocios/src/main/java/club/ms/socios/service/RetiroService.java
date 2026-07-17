package club.ms.socios.service;

import club.ms.socios.dto.AprobarRetiroRequest;
import club.ms.socios.dto.CrearRetiroRequest;
import club.ms.socios.dto.RetiroResponse;
import club.ms.socios.exception.BusinessRuleException;
import club.ms.socios.exception.ResourceNotFoundException;
import club.ms.socios.model.Socio;
import club.ms.socios.model.SolicitudRetiro;
import club.ms.socios.repository.SocioRepository;
import club.ms.socios.repository.SolicitudRetiroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RetiroService {

    private final SolicitudRetiroRepository solicitudRetiroRepository;
    private final SocioRepository socioRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${facturacion.service.url:http://ms-facturacion:8084}")
    private String facturacionServiceUrl;

    @Transactional
    public RetiroResponse solicitar(CrearRetiroRequest request) {
        Socio socio = socioRepository.findById(request.getIdSocio())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Socio no encontrado con id: " + request.getIdSocio()));

        solicitudRetiroRepository.findFirstBySocioAndEstadoSolicitud(socio, "Pendiente")
                .ifPresent(s -> {
                    throw new BusinessRuleException("Este socio ya tiene una solicitud de retiro en proceso.");
                });

        SolicitudRetiro solicitud = new SolicitudRetiro();
        solicitud.setSocio(socio);
        solicitud.setMotivo(request.getMotivo());
        solicitud.setEstadoSolicitud("Pendiente");
        SolicitudRetiro guardada = solicitudRetiroRepository.save(solicitud);

        return toResponse(guardada);
    }

    public List<RetiroResponse> listarPendientes(String bearerToken) {
        return solicitudRetiroRepository.findByEstadoSolicitudOrderByFechaSolicitudAsc("Pendiente")
                .stream()
                .map(s -> toResponse(s, consultarDeudaSinFallar(s.getSocio().getIdSocio(), bearerToken)))
                .toList();
    }

    // Version de solo-lectura para la lista: si falla la consulta a ms-facturacion
    // no bloquea la vista, simplemente muestra 0 (a diferencia de "aprobar", que
    // sí falla cerrado).
    private BigDecimal consultarDeudaSinFallar(Long idSocio, String bearerToken) {
        try {
            return consultarDeudaPendiente(idSocio, bearerToken);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Transactional
    public void aprobar(AprobarRetiroRequest request, String bearerToken) {
        SolicitudRetiro solicitud = solicitudRetiroRepository.findById(request.getIdSolicitud())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitud de retiro no encontrada con id: " + request.getIdSolicitud()));

        BigDecimal deuda = consultarDeudaPendiente(request.getIdSocio(), bearerToken);
        if (deuda.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException(
                    "No se puede dar de baja. El socio mantiene una deuda pendiente de S/ " + deuda);
        }

        solicitud.setEstadoSolicitud("Aprobada");
        solicitud.setFechaProcesamiento(LocalDateTime.now());
        solicitudRetiroRepository.save(solicitud);

        Socio socio = socioRepository.findById(request.getIdSocio())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Socio no encontrado con id: " + request.getIdSocio()));
        socio.setEstadoMembresia("Retirado");
        socioRepository.save(socio);
    }

    // Llamada servicio-a-servicio a ms-facturacion, reenviando el JWT del usuario
    // que hizo la petición original (Jefe = ROLE_1, que ya tiene acceso a ese
    // endpoint). Si ms-facturacion no responde, se falla "cerrado" (se asume que
    // SÍ hay deuda) para no dar de baja a un socio por error de red.
    private BigDecimal consultarDeudaPendiente(Long idSocio, String bearerToken) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(facturacionServiceUrl + "/api/facturas/deuda/" + idSocio))
                    .header("Authorization", bearerToken)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BusinessRuleException(
                        "No se pudo verificar la deuda del socio (ms-facturacion respondió " + response.statusCode() + "). Intenta de nuevo.");
            }
            Map<String, Object> body = objectMapper.readValue(response.body(), Map.class);
            return new BigDecimal(body.get("deuda").toString());
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("No se pudo verificar la deuda del socio: " + e.getMessage());
        }
    }

    private RetiroResponse toResponse(SolicitudRetiro s) {
        return toResponse(s, BigDecimal.ZERO);
    }

    private RetiroResponse toResponse(SolicitudRetiro s, BigDecimal deudaPendiente) {
        Socio socio = s.getSocio();
        return new RetiroResponse(
                s.getIdSolicitud(),
                socio.getIdSocio(),
                s.getMotivo(),
                s.getFechaSolicitud(),
                s.getEstadoSolicitud(),
                socio.getNombres(),
                socio.getApellidos(),
                socio.getDni(),
                deudaPendiente
        );
    }
}
