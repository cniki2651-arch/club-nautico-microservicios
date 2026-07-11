package auth_service.service;

import auth_service.dto.AuthResponse;
import auth_service.entity.RefreshToken;
import auth_service.entity.Usuario;
import auth_service.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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
     * Valida credenciales y retorna AuthResponse con Access Token y Refresh Token.
     */
    public AuthResponse login(String correo, String password) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        String accessToken = jwtService.generateToken(usuario);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }
}