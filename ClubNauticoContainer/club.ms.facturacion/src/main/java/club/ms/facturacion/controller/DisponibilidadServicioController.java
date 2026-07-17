package club.ms.facturacion.controller;

import club.ms.facturacion.dto.DisponibilidadServicioRequest;
import club.ms.facturacion.dto.DisponibilidadServicioResponse;
import club.ms.facturacion.service.DisponibilidadServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumos")
@RequiredArgsConstructor
public class DisponibilidadServicioController {

    private final DisponibilidadServicioService disponibilidadServicioService;

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadServicioResponse>> listar() {
        return ResponseEntity.ok(disponibilidadServicioService.listar());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @PutMapping("/disponibilidad/{servicio}")
    public ResponseEntity<DisponibilidadServicioResponse> actualizar(
            @PathVariable String servicio,
            @Valid @RequestBody DisponibilidadServicioRequest request) {
        return ResponseEntity.ok(disponibilidadServicioService.actualizar(servicio, request));
    }
}
