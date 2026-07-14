package club.ms.facturacion.controller;

import club.ms.facturacion.dto.CobranzaResponse;
import club.ms.facturacion.dto.MorosidadInteresResponse;
import club.ms.facturacion.service.CobranzaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cobranza")
@RequiredArgsConstructor
public class CobranzaController {

    private final CobranzaService cobranzaService;

    // Panel de cobranza: facturas vigentes proximas a vencer
    @GetMapping("/pendientes")
    public ResponseEntity<List<CobranzaResponse>> pendientesPorVencer() {
        return ResponseEntity.ok(cobranzaService.facturasPendientesPorVencer());
    }

    // Facturas vencidas con calculo de interes por mora
    @GetMapping("/vencidas")
    public ResponseEntity<List<CobranzaResponse>> vencidasConInteres() {
        return ResponseEntity.ok(cobranzaService.facturasVencidasConInteres());
    }

    // Historial de pagos ya registrados
    @GetMapping("/historial")
    public ResponseEntity<List<CobranzaResponse>> historialPagos() {
        return ResponseEntity.ok(cobranzaService.historialPagos());
    }

    // Historial de calculos de mora guardados para una factura especifica
    @GetMapping("/morosidad/{idFactura}")
    public ResponseEntity<List<MorosidadInteresResponse>> historialMorosidad(@PathVariable Long idFactura) {
        return ResponseEntity.ok(cobranzaService.historialMorosidadPorFactura(idFactura));
    }
}
