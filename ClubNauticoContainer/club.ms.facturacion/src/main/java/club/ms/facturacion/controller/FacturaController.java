package club.ms.facturacion.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import club.ms.facturacion.dto.FacturaRequest;
import club.ms.facturacion.dto.FacturaResponse;
import club.ms.facturacion.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PreAuthorize("hasAuthority('ROLE_4')")
    @GetMapping
    public ResponseEntity<List<FacturaResponse>> listar() {
        return ResponseEntity.ok(facturaService.listarTodas());
    }

    @PreAuthorize("hasAuthority('ROLE_4')")
    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.buscarPorId(id));
    }

    @PreAuthorize("hasAuthority('ROLE_4')")
    @PostMapping
    public ResponseEntity<FacturaResponse> crear(@Valid @RequestBody FacturaRequest request) {
        FacturaResponse creada = facturaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PreAuthorize("hasAuthority('ROLE_4')")
    @PutMapping("/{id}")
    public ResponseEntity<FacturaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FacturaRequest request) {
        return ResponseEntity.ok(facturaService.actualizar(id, request));
    }

    // Registra el pago de una factura y devuelve el desglose de interés moratorio
    // (usado por el panel de cobranza -- DashboardCobranza.tsx lee monto_base/
    // interes_sbs/dias_mora/total_pagado del resultado).
    @PreAuthorize("hasAuthority('ROLE_4')")
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<club.ms.facturacion.dto.PagoResponse> registrarPago(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate fechaPago) {
        LocalDate fecha = fechaPago != null ? fechaPago : LocalDate.now();
        return ResponseEntity.ok(facturaService.registrarPagoConDesglose(id, fecha));
    }

    // Usado por ms-nautica (via gateway) antes de autorizar un zarpe, y por el
    // panel de retiros para saber si un socio tiene saldo pendiente. La clave
    // "deuda" coincide con lo que el frontend ya sabe leer (ZarpesPage.tsx).
    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_3', 'ROLE_4')")
    @GetMapping("/deuda/{idSocio}")
    public ResponseEntity<java.util.Map<String, java.math.BigDecimal>> deudaPendiente(@PathVariable Long idSocio) {
        return ResponseEntity.ok(java.util.Map.of("deuda", facturaService.calcularDeudaPendiente(idSocio)));
    }

    @PreAuthorize("hasAuthority('ROLE_4')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        facturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_4')")
    @GetMapping("/dashboard")
    public ResponseEntity<club.ms.facturacion.dto.DashboardFinanzasResponse> dashboardFinanzas() {
        return ResponseEntity.ok(facturaService.dashboardFinanzas());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_4')")
    @GetMapping("/estados-cuenta")
    public ResponseEntity<List<club.ms.facturacion.dto.EstadoCuentaResponse>> estadosCuenta() {
        return ResponseEntity.ok(facturaService.estadosCuentaGeneral());
    }

    @PreAuthorize("hasAuthority('ROLE_4')")
    @PostMapping("/fraccionar")
    public ResponseEntity<club.ms.facturacion.dto.FraccionarResponse> fraccionar(
            @Valid @RequestBody club.ms.facturacion.dto.FraccionarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facturaService.fraccionar(request.getIdFactura(), request.getCuotas()));
    }

    // Consolida los consumos pendientes en facturas nuevas, una por socio
    // (version simplificada -- ver nota en FacturaService.generarFacturacionMensual)
    @PreAuthorize("hasAuthority('ROLE_4')")
    @PostMapping("/generar")
    public ResponseEntity<java.util.Map<String, Object>> generarFacturacionMensual() {
        return ResponseEntity.ok(facturaService.generarFacturacionMensual(null));
    }
}
