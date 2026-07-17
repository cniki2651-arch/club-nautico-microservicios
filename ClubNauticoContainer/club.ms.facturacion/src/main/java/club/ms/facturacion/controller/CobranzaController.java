package club.ms.facturacion.controller;

import club.ms.facturacion.dto.CobranzaResponse;
import club.ms.facturacion.dto.MorosidadInteresResponse;
import club.ms.facturacion.service.CobranzaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cobranza")
@RequiredArgsConstructor
public class CobranzaController {

    private final CobranzaService cobranzaService;

    // Panel de cobranza: facturas vigentes proximas a vencer
    @PreAuthorize("hasAnyAuthority('ROLE_1','ROLE_4')")
    @GetMapping("/pendientes")
    public ResponseEntity<List<CobranzaResponse>> pendientesPorVencer() {
        return ResponseEntity.ok(cobranzaService.facturasPendientesPorVencer());
    }

    // Facturas vencidas con calculo de interes por mora. Acepta ?tasa_mensual=X
    // (se interpreta como tasa ANUAL equivalente en este calculo simplificado).
    @PreAuthorize("hasAnyAuthority('ROLE_1','ROLE_4')")
    @GetMapping("/vencidas")
    public ResponseEntity<List<CobranzaResponse>> vencidasConInteres(
            @RequestParam(value = "tasa_mensual", required = false) Double tasaMensual) {
        return ResponseEntity.ok(cobranzaService.facturasVencidasConInteres(tasaMensual));
    }

    // Historial de pagos ya registrados
    @PreAuthorize("hasAnyAuthority('ROLE_1','ROLE_4')")
    @GetMapping("/historial")
    public ResponseEntity<List<CobranzaResponse>> historialPagos() {
        return ResponseEntity.ok(cobranzaService.historialPagos());
    }

    // Historial de calculos de mora guardados para una factura especifica
    @PreAuthorize("hasAnyAuthority('ROLE_1','ROLE_4')")
    @GetMapping("/morosidad/{idFactura}")
    public ResponseEntity<List<MorosidadInteresResponse>> historialMorosidad(@PathVariable Long idFactura) {
        return ResponseEntity.ok(cobranzaService.historialMorosidadPorFactura(idFactura));
    }
}
