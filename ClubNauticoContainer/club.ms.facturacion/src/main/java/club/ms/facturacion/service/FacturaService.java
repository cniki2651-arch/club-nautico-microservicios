package club.ms.facturacion.service;

import club.ms.facturacion.dto.FacturaRequest;
import club.ms.facturacion.dto.FacturaResponse;
import club.ms.facturacion.dto.PagoResponse;
import club.ms.facturacion.exception.ResourceNotFoundException;
import club.ms.facturacion.model.Consumo;
import club.ms.facturacion.model.Factura;
import club.ms.facturacion.repository.ConsumoRepository;
import club.ms.facturacion.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final ConsumoRepository consumoRepository;
    private final CobranzaService cobranzaService;

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

    // Igual que registrarPago, pero además calcula y devuelve el desglose de
    // interés moratorio (usado por el panel de Cobranza para mostrar el detalle).
    public PagoResponse registrarPagoConDesglose(Long id, java.time.LocalDate fechaPago) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + id));

        long diasMora = cobranzaService.calcularDiasMora(factura.getFechaVencimiento());
        java.math.BigDecimal interes = diasMora > 0
                ? cobranzaService.calcularInteresParaFactura(factura.getMontoBase(), factura.getFechaVencimiento())
                : java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalPagado = factura.getMontoBase().add(interes);

        factura.setEstadoPago("PAGADO");
        factura.setFechaPago(fechaPago);
        factura.setMontoTotal(totalPagado);
        facturaRepository.save(factura);

        return new PagoResponse(factura.getMontoBase(), interes, diasMora, totalPagado);
    }

    // Suma el monto total de las facturas no pagadas de un socio (usado por
    // ms-nautica antes de autorizar un zarpe, y por el flujo de retiro).
    public java.math.BigDecimal calcularDeudaPendiente(Long idSocio) {
        return facturaRepository.findByIdSocio(idSocio).stream()
                .filter(f -> !"PAGADO".equals(f.getEstadoPago()))
                .map(Factura::getMontoTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // Deuda total agrupada por socio, para la vista de "Cuentas de Socios".
    public List<club.ms.facturacion.dto.EstadoCuentaResponse> estadosCuentaGeneral() {
        return facturaRepository.findAll().stream()
                .filter(f -> !"PAGADO".equals(f.getEstadoPago()))
                .collect(Collectors.groupingBy(Factura::getIdSocio))
                .entrySet().stream()
                .map(entry -> {
                    BigDecimal total = entry.getValue().stream()
                            .map(Factura::getMontoTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    boolean tieneVencida = entry.getValue().stream()
                            .anyMatch(f -> f.getFechaVencimiento().isBefore(java.time.LocalDate.now()));
                    String estado = tieneVencida ? "Moroso" : "Al día";
                    return new club.ms.facturacion.dto.EstadoCuentaResponse(entry.getKey(), total, estado);
                })
                .sorted(java.util.Comparator.comparing(club.ms.facturacion.dto.EstadoCuentaResponse::getIdSocio))
                .toList();
    }

    public void eliminar(Long id) {
        if (!facturaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Factura no encontrada con id: " + id);
        }
        facturaRepository.deleteById(id);
    }

    // Divide una factura VIGENTE en N cuotas (2-6), cada una venciendo un mes
    // despues de la anterior. La factura original queda marcada "FRACCIONADA".
    public club.ms.facturacion.dto.FraccionarResponse fraccionar(Long idFactura, int cuotas) {
        Factura original = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + idFactura));

        if ("PAGADO".equals(original.getEstadoPago()) || "FRACCIONADA".equals(original.getEstadoPago())) {
            throw new club.ms.facturacion.exception.BusinessRuleException(
                    "La factura ya está pagada o ya fue fraccionada.");
        }

        java.math.BigDecimal montoPorCuota = original.getMontoTotal()
                .divide(java.math.BigDecimal.valueOf(cuotas), 2, java.math.RoundingMode.HALF_UP);

        original.setEstadoPago("FRACCIONADA");
        facturaRepository.save(original);

        for (int i = 1; i <= cuotas; i++) {
            Factura cuota = new Factura();
            cuota.setIdSocio(original.getIdSocio());
            cuota.setConcepto("Fraccionamiento - Cuota " + i + "/" + cuotas);
            cuota.setMontoBase(montoPorCuota);
            cuota.setMontoTotal(montoPorCuota);
            cuota.setFechaEmision(java.time.LocalDate.now());
            cuota.setFechaVencimiento(original.getFechaEmision().plusMonths(i));
            cuota.setEstadoPago("VIGENTE");
            cuota.setIdUsuarioEmisor(original.getIdUsuarioEmisor());
            cuota.setFacturaPadre(original);
            cuota.setNumeroCuota(i);
            facturaRepository.save(cuota);
        }

        return new club.ms.facturacion.dto.FraccionarResponse(
                "Deuda fraccionada exitosamente en " + cuotas + " cuotas.", cuotas, montoPorCuota);
    }

    // KPIs y datos de la gráfica para el panel de Finanzas.
    public club.ms.facturacion.dto.DashboardFinanzasResponse dashboardFinanzas() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        List<Factura> todas = facturaRepository.findAll();

        BigDecimal pendienteFacturar = consumoRepository.findByFacturaIsNull().stream()
                .map(Consumo::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal facturadoPorCobrar = todas.stream()
                .filter(f -> "VIGENTE".equals(f.getEstadoPago()) && !f.getFechaVencimiento().isBefore(hoy))
                .map(Factura::getMontoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal morosidadTotal = todas.stream()
                .filter(f -> "VIGENTE".equals(f.getEstadoPago()) && f.getFechaVencimiento().isBefore(hoy))
                .map(Factura::getMontoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deudaFraccionada = todas.stream()
                .filter(f -> "FRACCIONADA".equals(f.getEstadoPago()) || f.getFacturaPadre() != null)
                .map(Factura::getMontoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        var kpis = new club.ms.facturacion.dto.DashboardFinanzasResponse.Kpis(
                pendienteFacturar, facturadoPorCobrar, morosidadTotal);

        var grafica = List.of(
                new club.ms.facturacion.dto.DashboardFinanzasResponse.GraficaItem("Por Cobrar (A tiempo)", facturadoPorCobrar),
                new club.ms.facturacion.dto.DashboardFinanzasResponse.GraficaItem("Morosidad", morosidadTotal),
                new club.ms.facturacion.dto.DashboardFinanzasResponse.GraficaItem("Deuda Fraccionada", deudaFraccionada)
        );

        return new club.ms.facturacion.dto.DashboardFinanzasResponse(kpis, grafica);
    }

    // Version simplificada de "generar facturacion mensual": consolida, por
    // socio, todos los consumos sin facturar en UNA factura nueva. A diferencia
    // del backend monolitico original, NO agrega el rubro de membresia fija ni
    // el cobro de radas (esos datos viven en ms-socios/ms-nautica, otro
    // microservicio, y no se implemento esa llamada cruzada por el tiempo
    // disponible) -- solo consolida lo que ya vive en esta base de datos.
    public java.util.Map<String, Object> generarFacturacionMensual(Integer idUsuarioEmisor) {
        List<Consumo> pendientes = consumoRepository.findByFacturaIsNull();
        var porSocio = pendientes.stream().collect(Collectors.groupingBy(Consumo::getIdSocio));

        int facturasGeneradas = 0;
        for (var entry : porSocio.entrySet()) {
            Long idSocio = entry.getKey();
            List<Consumo> consumosSocio = entry.getValue();
            BigDecimal total = consumosSocio.stream()
                    .map(Consumo::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Factura factura = new Factura();
            factura.setIdSocio(idSocio);
            factura.setConcepto("Facturación mensual - consumos consolidados");
            factura.setMontoBase(total);
            factura.setMontoTotal(total);
            factura.setFechaEmision(java.time.LocalDate.now());
            factura.setFechaVencimiento(java.time.LocalDate.now().plusDays(15));
            factura.setEstadoPago("VIGENTE");
            factura.setIdUsuarioEmisor(idUsuarioEmisor);
            Factura guardada = facturaRepository.save(factura);

            for (Consumo c : consumosSocio) {
                c.setFactura(guardada);
                c.setEstado("FACTURADO");
                consumoRepository.save(c);
            }
            facturasGeneradas++;
        }

        return java.util.Map.of(
                "mensaje", "Se generaron " + facturasGeneradas + " factura(s).",
                "facturas_generadas", facturasGeneradas
        );
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
