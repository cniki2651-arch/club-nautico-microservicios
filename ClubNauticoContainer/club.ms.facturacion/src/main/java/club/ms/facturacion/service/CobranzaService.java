package club.ms.facturacion.service;

import club.ms.facturacion.dto.CobranzaResponse;
import club.ms.facturacion.dto.MorosidadInteresResponse;
import club.ms.facturacion.model.Factura;
import club.ms.facturacion.model.MorosidadInteres;
import club.ms.facturacion.repository.FacturaRepository;
import club.ms.facturacion.repository.MorosidadInteresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Calcula y registra el historial de morosidad/intereses de las facturas vencidas.
 * "facturas" y "morosidad_intereses" son tablas reales; el resto de reportes de cobranza
 * (pendientes, historial de pagos) se arman leyendo directamente sobre "facturas".
 */
@Service
@RequiredArgsConstructor
public class CobranzaService {

    private final FacturaRepository facturaRepository;
    private final MorosidadInteresRepository morosidadInteresRepository;

    // Tasa anual usada para calcular el interes moratorio (simplificado con fines academicos,
    // una tasa SBS real se publica periodicamente y varia por tipo de credito).
    @Value("${cobranza.tasa-interes-anual:0.15}")
    private double tasaInteresAnual;

    // Facturas vigentes que aun no vencen (pago anticipado / al dia)
    public List<CobranzaResponse> facturasPendientesPorVencer() {
        LocalDate hoy = LocalDate.now();
        return facturaRepository.findByEstadoPago("VIGENTE")
                .stream()
                .filter(f -> !f.getFechaVencimiento().isBefore(hoy))
                .map(f -> toCobranzaResponse(f, BigDecimal.ZERO, 0L))
                .toList();
    }

    // Facturas vencidas: calcula el interes y GUARDA un registro en morosidad_intereses
    public List<CobranzaResponse> facturasVencidasConInteres() {
        return facturasVencidasConInteres(null);
    }

    // Version con tasa anual configurable (si no se pasa, usa la tasa por defecto)
    public List<CobranzaResponse> facturasVencidasConInteres(Double tasaAnualOverride) {
        LocalDate hoy = LocalDate.now();
        return facturaRepository.findByEstadoPago("VIGENTE")
                .stream()
                .filter(f -> f.getFechaVencimiento().isBefore(hoy))
                .map(f -> calcularYRegistrarMorosidad(f, tasaAnualOverride))
                .toList();
    }

    // Historial de facturas ya pagadas
    public List<CobranzaResponse> historialPagos() {
        return facturaRepository.findByEstadoPago("PAGADO")
                .stream()
                .map(f -> toCobranzaResponse(f, BigDecimal.ZERO, 0L))
                .toList();
    }

    // Historial de calculos de morosidad ya guardados para una factura especifica
    public List<MorosidadInteresResponse> historialMorosidadPorFactura(Long idFactura) {
        return morosidadInteresRepository.findByFacturaIdFactura(idFactura)
                .stream()
                .map(m -> new MorosidadInteresResponse(
                        m.getIdMorosidad(),
                        m.getFactura().getIdFactura(),
                        m.getDiasRetraso(),
                        m.getMontoInteresGenerado(),
                        m.getFechaCalculo()
                ))
                .toList();
    }

    // Calcula el interes de una factura vencida y persiste el registro en morosidad_intereses
    private CobranzaResponse calcularYRegistrarMorosidad(Factura factura, Double tasaAnualOverride) {
        LocalDate hoy = LocalDate.now();
        long diasMora = ChronoUnit.DAYS.between(factura.getFechaVencimiento(), hoy);
        double tasa = tasaAnualOverride != null ? tasaAnualOverride : tasaInteresAnual;
        BigDecimal interes = calcularInteresMoratorio(factura.getMontoBase(), diasMora, tasa);

        MorosidadInteres registro = new MorosidadInteres();
        registro.setFactura(factura);
        registro.setDiasRetraso((int) diasMora);
        registro.setMontoInteresGenerado(interes);
        registro.setFechaCalculo(hoy);
        morosidadInteresRepository.save(registro);

        return toCobranzaResponse(factura, interes, diasMora);
    }

    private CobranzaResponse toCobranzaResponse(Factura factura, BigDecimal interes, long diasMora) {
        BigDecimal totalAcumulado = factura.getMontoBase().add(interes);
        return new CobranzaResponse(
                factura.getIdFactura(),
                factura.getIdSocio(),
                factura.getConcepto(),
                factura.getMontoBase(),
                interes,
                totalAcumulado,
                factura.getFechaVencimiento(),
                diasMora,
                factura.getEstadoPago()
        );
    }

    // Expuesto para que FacturaService pueda calcular el desglose de interes al
    // momento de registrar un pago (usado por el panel de Cobranza).
    public BigDecimal calcularInteresParaFactura(BigDecimal montoBase, LocalDate fechaVencimiento) {
        long diasMora = ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now());
        return calcularInteresMoratorio(montoBase, diasMora, tasaInteresAnual);
    }

    public long calcularDiasMora(LocalDate fechaVencimiento) {
        long dias = ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now());
        return Math.max(dias, 0);
    }

    // Formula de interes compuesto simplificada: monto * ((1 + tasaAnual)^(dias/360) - 1)
    private BigDecimal calcularInteresMoratorio(BigDecimal montoBase, long diasMora, double tasaAnual) {
        if (diasMora <= 0) {
            return BigDecimal.ZERO;
        }
        double factor = Math.pow(1 + tasaAnual, diasMora / 360.0) - 1;
        BigDecimal interes = montoBase.multiply(BigDecimal.valueOf(factor));
        return interes.setScale(2, RoundingMode.HALF_UP);
    }
}
