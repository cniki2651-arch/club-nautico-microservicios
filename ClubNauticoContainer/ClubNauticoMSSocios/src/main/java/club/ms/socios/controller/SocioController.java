package club.ms.socios.controller;

import club.ms.socios.dto.SocioRequest;
import club.ms.socios.dto.SocioResponse;
import club.ms.socios.service.SocioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/socios")
@RequiredArgsConstructor
public class SocioController {

    private final SocioService socioService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<SocioResponse>> listar() {
        return ResponseEntity.ok(socioService.listarTodos());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @GetMapping("/{id}")
    public ResponseEntity<SocioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(socioService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @GetMapping("/buscar")
    public ResponseEntity<SocioResponse> buscarPorDocumento(
            @RequestParam("tipo_doc") String tipoDoc,
            @RequestParam String numero) {
        return ResponseEntity.ok(socioService.buscarPorDocumento(tipoDoc, numero));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @PostMapping
    public ResponseEntity<SocioResponse> crear(@Valid @RequestBody SocioRequest request) {
        SocioResponse creado = socioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @PutMapping("/{id}")
    public ResponseEntity<SocioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SocioRequest request) {
        return ResponseEntity.ok(socioService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        socioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
