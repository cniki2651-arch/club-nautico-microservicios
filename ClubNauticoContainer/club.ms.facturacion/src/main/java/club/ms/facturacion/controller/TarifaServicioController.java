package club.ms.facturacion.controller;

import club.ms.facturacion.dto.TarifaServicioResponse;
import club.ms.facturacion.service.TarifaServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/consumos")
@RequiredArgsConstructor
public class TarifaServicioController {

    private final TarifaServicioService tarifaServicioService;

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @GetMapping("/precios")
    public ResponseEntity<List<TarifaServicioResponse>> listarPrecios() {
        return ResponseEntity.ok(tarifaServicioService.listarActivas());
    }
}
