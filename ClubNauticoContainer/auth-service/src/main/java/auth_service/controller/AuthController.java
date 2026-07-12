package auth_service.controller;

import auth_service.dto.AuthResponse;
import auth_service.dto.LoginRequest;
import auth_service.dto.RefreshTokenRequest;
import auth_service.dto.RegisterRequest;
import auth_service.entity.RefreshToken;
import auth_service.entity.Usuario;
import auth_service.repository.RefreshTokenRepository;
import auth_service.service.AuthService;
import auth_service.service.JwtService;
import auth_service.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService,
                          RefreshTokenRepository refreshTokenRepository,
                          JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    /**
     * Registra un nuevo usuario.
     * Retorna accessToken + refreshToken.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(request.getPassword());
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setRol(request.getRol());

        AuthResponse response = authService.register(usuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Autentica un usuario con correo + contraseña.
     * Retorna accessToken + refreshToken.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getCorreo(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    /**
     * Recibe un Refresh Token válido y emite un nuevo Access Token.
     * Implementa rotación: el refresh token anterior se invalida y se emite uno nuevo.
     *
     * Body: { "refreshToken": "uuid-del-refresh-token" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh Token inválido o no encontrado"));

        // Verificar expiración (elimina y lanza excepción si expiró)
        refreshTokenService.verifyExpiration(refreshToken);

        // Obtener datos del usuario asociado
        Usuario usuario = refreshToken.getUsuario();
        Integer roleId = Integer.parseInt(usuario.getRol().replace("ROLE_", ""));

        // Generar nuevo Access Token
        String newAccessToken = jwtService.generateTokenFromCorreo(usuario.getCorreo(), roleId);

        // Rotación: crear nuevo refresh token e invalidar el anterior
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(usuario);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken.getToken()));
    }

    /**
     * Invalida el Refresh Token del usuario autenticado (cierre de sesión).
     * Requiere token JWT válido en el header Authorization.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal String correo) {
        // AuthenticationPrincipal extrae el principal (correo) del SecurityContext
        // inyectado por JwtAuthenticationFilter
        if (correo != null) {
            // Buscar usuario por correo a través del refresh token (lazy load)
            refreshTokenRepository.findAll().stream()
                .filter(rt -> rt.getUsuario().getCorreo().equals(correo))
                .findFirst()
                .ifPresent(rt -> refreshTokenService.deleteByUsuario(rt.getUsuario()));
        }
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}