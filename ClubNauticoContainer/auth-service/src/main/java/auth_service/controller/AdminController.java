package auth_service.controller;

import auth_service.entity.Usuario;
import auth_service.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;

    public AdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("hasAuthority('ROLE_1')")
    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // Cambiar rol de un usuario por su ID
    @PreAuthorize("hasAuthority('ROLE_1')")
    @PutMapping("/usuarios/{id}/rol")
    public ResponseEntity<String> cambiarRol(@PathVariable Long id, @RequestBody String nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setRol(nuevoRol); 
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Rol actualizado a " + nuevoRol);
    }
}