package club.ms.facturacion.service;

import club.ms.facturacion.dto.FacturaRequest;
import club.ms.facturacion.dto.FacturaResponse;
import club.ms.facturacion.exception.ResourceNotFoundException;
import club.ms.facturacion.model.Factura;
import club.ms.facturacion.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public List<FacturaResponse> listarTodas() {
        return facturaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FacturaResponse buscarPorId(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + id));
        return toResponse(factura);
    }

    public FacturaResponse crear(FacturaRequest request) {
        Factura factura = toEntity(request, new Factura());
        if (factura.getEstadoPago() == null) {
            factura.setEstadoPago("VIGENTE");
        }
        return toResponse(facturaRepository.save(factura));
    }

    public FacturaResponse actualizar(Long id, FacturaRequest request) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + id));
        Factura actualizada = toEntity(request, factura);
        return toResponse(facturaRepository.save(actualizada));
    }

    // Marca una factura como pagada (usado por el panel de cobranza)
    public FacturaResponse registrarPago(Long id, java.time.LocalDate fechaPago) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + id));
        factura.setEstadoPago("PAGADO");
        factura.setFechaPago(fechaPago);
        return toResponse(facturaRepository.save(factura));
    }

    public void eliminar(Long id) {
        if (!facturaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Factura no encontrada con id: " + id);
        }
        facturaRepository.deleteById(id);
    }

    private Factura toEntity(FacturaRequest request, Factura factura) {
        factura.setIdSocio(request.getIdSocio());
        factura.setConcepto(request.getConcepto());
        factura.setMontoBase(request.getMontoBase());
        factura.setMontoTotal(request.getMontoTotal());
        factura.setFechaEmision(request.getFechaEmision());
        factura.setFechaVencimiento(request.getFechaVencimiento());
        if (request.getEstadoPago() != null) {
            factura.setEstadoPago(request.getEstadoPago());
        }
        factura.setIdUsuarioEmisor(request.getIdUsuarioEmisor());
        factura.setNumeroCuota(request.getNumeroCuota());
        factura.setFechaPago(request.getFechaPago());

        if (request.getIdFacturaPadre() != null) {
            Factura padre = facturaRepository.findById(request.getIdFacturaPadre())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Factura padre no encontrada con id: " + request.getIdFacturaPadre()));
            factura.setFacturaPadre(padre);
        } else {
            factura.setFacturaPadre(null);
        }
        return factura;
    }

    private FacturaResponse toResponse(Factura factura) {
        return new FacturaResponse(
                factura.getIdFactura(),
                factura.getIdSocio(),
                factura.getConcepto(),
                factura.getMontoBase(),
                factura.getMontoTotal(),
                factura.getFechaEmision(),
                factura.getFechaVencimiento(),
                factura.getEstadoPago(),
                factura.getIdUsuarioEmisor(),
                factura.getFacturaPadre() != null ? factura.getFacturaPadre().getIdFactura() : null,
                factura.getNumeroCuota(),
                factura.getFechaPago()
        );
    }
}
