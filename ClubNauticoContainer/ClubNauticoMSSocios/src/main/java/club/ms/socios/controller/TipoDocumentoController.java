package club.ms.socios.controller;

import club.ms.socios.dto.TipoDocumentoResponse;
import club.ms.socios.service.TipoDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-documento")
@RequiredArgsConstructor
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<TipoDocumentoResponse>> listar() {
        return ResponseEntity.ok(tipoDocumentoService.listarTodos());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_1', 'ROLE_2')")
    @GetMapping("/{id}")
    public ResponseEntity<TipoDocumentoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoDocumentoService.buscarPorId(id));
    }
}
