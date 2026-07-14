package club.ms.facturacion.controller;

import club.ms.facturacion.dto.ConsumoRequest;
import club.ms.facturacion.dto.ConsumoResponse;
import club.ms.facturacion.service.ConsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumos")
@RequiredArgsConstructor
public class ConsumoController {

    private final ConsumoService consumoService;

    @GetMapping
    public ResponseEntity<List<ConsumoResponse>> listar() {
        return ResponseEntity.ok(consumoService.listarTodos());
    }

    @GetMapping("/sin-facturar")
    public ResponseEntity<List<ConsumoResponse>> listarSinFacturar() {
        return ResponseEntity.ok(consumoService.listarSinFacturar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsumoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consumoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ConsumoResponse> crear(@Valid @RequestBody ConsumoRequest request) {
        ConsumoResponse creado = consumoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsumoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ConsumoRequest request) {
        return ResponseEntity.ok(consumoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        consumoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
