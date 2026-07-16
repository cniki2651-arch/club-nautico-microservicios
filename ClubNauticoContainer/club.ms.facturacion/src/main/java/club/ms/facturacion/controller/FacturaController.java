package club.ms.facturacion.controller;

import club.ms.facturacion.dto.FacturaRequest;
import club.ms.facturacion.dto.FacturaResponse;
import club.ms.facturacion.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    // Registra el pago de una factura (usado por el panel de cobranza)
    @PreAuthorize("hasAuthority('ROLE_4')")
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<FacturaResponse> registrarPago(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate fechaPago) {
        LocalDate fecha = fechaPago != null ? fechaPago : LocalDate.now();
        return ResponseEntity.ok(facturaService.registrarPago(id, fecha));
    }

    @PreAuthorize("hasAuthority('ROLE_4')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        facturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
