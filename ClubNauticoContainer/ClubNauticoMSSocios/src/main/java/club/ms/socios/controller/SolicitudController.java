package club.ms.socios.controller;

import club.ms.socios.dto.CrearSolicitudRequest;
import club.ms.socios.dto.EvaluarSolicitudRequest;
import club.ms.socios.dto.SolicitudResponse;
import club.ms.socios.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> listar() {
        return ResponseEntity.ok(solicitudService.listar());
    }

    @PreAuthorize("hasAuthority('ROLE_2')")
    @PostMapping("/crear")
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody CrearSolicitudRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(request));
    }

    @PreAuthorize("hasAuthority('ROLE_1')")
    @PutMapping("/{id}/evaluar")
    public ResponseEntity<Void> evaluar(
            @PathVariable Long id,
            @Valid @RequestBody EvaluarSolicitudRequest request,
            Authentication authentication) {
        solicitudService.evaluar(id, request, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
