package auth_service.controller;

import auth_service.dto.LoginRequest;
import auth_service.dto.RegisterRequest;
import auth_service.entity.Usuario;
import auth_service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(request.getPassword());
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setRol(request.getRol());
        authService.register(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getCorreo(), request.getPassword());
        return ResponseEntity.ok(token);
    }
}