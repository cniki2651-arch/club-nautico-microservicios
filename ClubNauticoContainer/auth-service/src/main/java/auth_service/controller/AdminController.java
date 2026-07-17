package auth_service.controller;

import auth_service.entity.Permiso;
import auth_service.entity.Rol;
import auth_service.entity.Usuario;
import auth_service.repository.PermisoRepository;
import auth_service.repository.RolRepository;
import auth_service.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public AdminController(UsuarioRepository usuarioRepository, RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    @PreAuthorize("hasAuthority('ROLE_1')")
    @GetMapping("/roles")
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ROLE_1')")
    @GetMapping("/permisos")
    public List<Permiso> listarPermisos() {
        return permisoRepository.findAll();
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

    // Editar nombres/apellidos/rol de un usuario (edicion completa, usada por el
    // modal de "Gestion de Usuarios" del frontend)
    @PreAuthorize("hasAuthority('ROLE_1')")
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> editarUsuario(
            @PathVariable Long id,
            @RequestBody auth_service.dto.EditarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.getNombres() != null) usuario.setNombres(request.getNombres());
        if (request.getApellidos() != null) usuario.setApellidos(request.getApellidos());
        if (request.getIdRol() != null) usuario.setRol(String.valueOf(request.getIdRol()));

        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasAuthority('ROLE_1')")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}