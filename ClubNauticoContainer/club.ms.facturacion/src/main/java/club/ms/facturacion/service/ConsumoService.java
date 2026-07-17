package club.ms.facturacion.service;

import club.ms.facturacion.dto.ConsumoDetalleResponse;
import club.ms.facturacion.dto.ConsumoRequest;
import club.ms.facturacion.dto.ConsumoResponse;
import club.ms.facturacion.dto.SocioConConsumosResponse;
import club.ms.facturacion.exception.ResourceNotFoundException;
import club.ms.facturacion.model.Consumo;
import club.ms.facturacion.model.Factura;
import club.ms.facturacion.repository.ConsumoRepository;
import club.ms.facturacion.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumoService {

    private final ConsumoRepository consumoRepository;
    private final FacturaRepository facturaRepository;

    public List<ConsumoResponse> listarTodos() {
        return consumoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ConsumoResponse buscarPorId(Long id) {
        Consumo consumo = consumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consumo no encontrado con id: " + id));
        return toResponse(consumo);
    }

    // Consumos que aun no se agruparon en ninguna factura (utiles para armar una factura nueva)
    public List<ConsumoResponse> listarSinFacturar() {
        return consumoRepository.findByFacturaIsNull()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ConsumoResponse crear(ConsumoRequest request) {
        Consumo consumo = toEntity(request, new Consumo());
        if (consumo.getEstado() == null) {
            consumo.setEstado("PENDIENTE");
        }
        if (consumo.getFechaConsumo() == null) {
            consumo.setFechaConsumo(LocalDateTime.now());
        }
        return toResponse(consumoRepository.save(consumo));
    }

    public ConsumoResponse actualizar(Long id, ConsumoRequest request) {
        Consumo consumo = consumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consumo no encontrado con id: " + id));
        Consumo actualizado = toEntity(request, consumo);
        return toResponse(consumoRepository.save(actualizado));
    }

    // Agrupa los consumos sin facturar por socio (usado por la pantalla de
    // Facturación para armar la facturación mensual).
    public List<SocioConConsumosResponse> listarSinFacturarAgrupados() {
        return consumoRepository.findByFacturaIsNull().stream()
                .collect(Collectors.groupingBy(Consumo::getIdSocio))
                .entrySet().stream()
                .map(entry -> new SocioConConsumosResponse(
                        entry.getKey(),
                        entry.getValue().stream().map(Consumo::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add),
                        entry.getValue().stream()
                                .map(c -> new ConsumoDetalleResponse(
                                        c.getIdConsumo(), c.getServicio(), c.getMonto(), c.getDescripcion(), c.getFechaConsumo()))
                                .toList()
                ))
                .sorted(Comparator.comparing(SocioConConsumosResponse::getIdSocio))
                .toList();
    }

    public void eliminar(Long id) {
        if (!consumoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consumo no encontrado con id: " + id);
        }
        consumoRepository.deleteById(id);
    }

    private Consumo toEntity(ConsumoRequest request, Consumo consumo) {
        consumo.setIdSocio(request.getIdSocio());
        consumo.setServicio(request.getServicio());
        consumo.setMonto(request.getMonto());
        consumo.setDescripcion(request.getDescripcion());
        if (request.getEstado() != null) {
            consumo.setEstado(request.getEstado());
        }
        if (request.getFechaConsumo() != null) {
            consumo.setFechaConsumo(request.getFechaConsumo());
        }
        consumo.setIdUsuarioRegistro(request.getIdUsuarioRegistro());

        if (request.getIdFactura() != null) {
            Factura factura = facturaRepository.findById(request.getIdFactura())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Factura no encontrada con id: " + request.getIdFactura()));
            consumo.setFactura(factura);
        } else {
            consumo.setFactura(null);
        }
        return consumo;
    }

    private ConsumoResponse toResponse(Consumo consumo) {
        return new ConsumoResponse(
                consumo.getIdConsumo(),
                consumo.getIdSocio(),
                consumo.getServicio(),
                consumo.getMonto(),
                consumo.getDescripcion(),
                consumo.getEstado(),
                consumo.getFechaConsumo(),
                consumo.getIdUsuarioRegistro(),
                consumo.getFactura() != null ? consumo.getFactura().getIdFactura() : null
        );
    }
}
