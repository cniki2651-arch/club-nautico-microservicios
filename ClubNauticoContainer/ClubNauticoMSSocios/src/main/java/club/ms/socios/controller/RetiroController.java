package club.ms.socios.controller;

import club.ms.socios.dto.AprobarRetiroRequest;
import club.ms.socios.dto.CrearRetiroRequest;
import club.ms.socios.dto.RetiroResponse;
import club.ms.socios.service.RetiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retiros")
@RequiredArgsConstructor
public class RetiroController {

    private final RetiroService retiroService;

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @PostMapping("/solicitar")
    public ResponseEntity<RetiroResponse> solicitar(@Valid @RequestBody CrearRetiroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(retiroService.solicitar(request));
    }

    @PreAuthorize("hasAuthority('ROLE_1')")
    @GetMapping("/pendientes")
    public ResponseEntity<List<RetiroResponse>> pendientes(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(retiroService.listarPendientes(authorization));
    }

    @PreAuthorize("hasAuthority('ROLE_1')")
    @PostMapping("/aprobar")
    public ResponseEntity<Void> aprobar(
            @Valid @RequestBody AprobarRetiroRequest request,
            @RequestHeader("Authorization") String authorization) {
        retiroService.aprobar(request, authorization);
        return ResponseEntity.ok().build();
    }
}
