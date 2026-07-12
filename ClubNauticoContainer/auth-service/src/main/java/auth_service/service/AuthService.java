package auth_service.service;

import auth_service.dto.AuthResponse;
import auth_service.entity.RefreshToken;
import auth_service.entity.Usuario;
import auth_service.repository.UsuarioRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Registra un nuevo usuario, encripta su contraseña y retorna
     * un AuthResponse con el Access Token y Refresh Token generados.
     */
    public AuthResponse register(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario saved = usuarioRepository.save(usuario);

        String accessToken = jwtService.generateToken(saved);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(saved);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    /**
     * Valida credenciales con protección contra fuerza bruta.
     *
     * Flujo:
     *  1. Verificar si la cuenta está bloqueada por exceso de intentos.
     *  2. Buscar usuario por correo (fallo genérico para no revelar si existe).
     *  3. Verificar contraseña.
     *  4. En fallo: incrementar contador. En éxito: resetear contador.
     *  5. Retornar AuthResponse con Access Token + Refresh Token.
     */
    public AuthResponse login(String correo, String password) {
        // ── 1. Verificar bloqueo por fuerza bruta ──────────────────────────────
        if (loginAttemptService.isBlocked(correo)) {
            long minutosRestantes = loginAttemptService.getRemainingBlockMinutes(correo);
            throw new LockedException(
                "Cuenta bloqueada temporalmente por demasiados intentos fallidos. " +
                "Por favor, espera " + minutosRestantes + " minuto(s) antes de intentarlo de nuevo."
            );
        }

        // ── 2. Buscar usuario ──────────────────────────────────────────────────
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

        // ── 3. Verificar contraseña ────────────────────────────────────────────
        // Usamos un mensaje genérico para no revelar si el correo existe o no (seguridad por oscuridad)
        if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())) {
            loginAttemptService.loginFailed(correo);
            throw new RuntimeException("Correo o contraseña incorrectos");
        }

        // ── 4. Login exitoso: resetear contador ────────────────────────────────
        loginAttemptService.loginSucceeded(correo);

        // ── 5. Generar tokens ──────────────────────────────────────────────────
        String accessToken = jwtService.generateToken(usuario);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }
}