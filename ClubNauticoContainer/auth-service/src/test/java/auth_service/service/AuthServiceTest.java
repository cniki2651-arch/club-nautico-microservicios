package auth_service.service;

import auth_service.dto.AuthResponse;
import auth_service.entity.RefreshToken;
import auth_service.entity.Usuario;
import auth_service.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Equivalente Java de CP-CN-LOG-02 (loginValidacion.test.js del backend monolitico original):
// "Rechazar credenciales incorrectas" -- debe fallar con el mismo mensaje generico tanto si
// la contrasena no coincide como si el usuario no existe (para no revelar cual de las dos paso).
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private LoginAttemptService loginAttemptService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService, refreshTokenService, loginAttemptService);
    }

    @Test
    void loginConContrasenaIncorrectaLanzaMensajeGenerico() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("secretaria@clubnautico.test");
        usuario.setPassword("$2b$10$hasheada");
        usuario.setRol("2");

        when(loginAttemptService.isBlocked("secretaria@clubnautico.test")).thenReturn(false);
        when(usuarioRepository.findByCorreo("secretaria@clubnautico.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("contrasena-incorrecta", "$2b$10$hasheada")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("secretaria@clubnautico.test", "contrasena-incorrecta"));

        assertEquals("Correo o contraseña incorrectos", ex.getMessage());
        verify(loginAttemptService).loginFailed("secretaria@clubnautico.test");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginConUsuarioInexistenteLanzaElMismoMensajeGenerico() {
        when(loginAttemptService.isBlocked("noexiste@clubnautico.test")).thenReturn(false);
        when(usuarioRepository.findByCorreo("noexiste@clubnautico.test")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login("noexiste@clubnautico.test", "cualquier123"));

        assertEquals("Correo o contraseña incorrectos", ex.getMessage());
        verify(loginAttemptService).loginFailed("noexiste@clubnautico.test");
    }

    @Test
    void loginConCredencialesCorrectasRetornaTokens() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("jefe@clubposeidon.com");
        usuario.setPassword("$2b$10$hasheada");
        usuario.setRol("1");

        when(loginAttemptService.isBlocked("jefe@clubposeidon.com")).thenReturn(false);
        when(usuarioRepository.findByCorreo("jefe@clubposeidon.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveCorrecta", "$2b$10$hasheada")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("access-token-simulado");
        when(refreshTokenService.createRefreshToken(usuario))
                .thenReturn(new RefreshToken("refresh-token-simulado", usuario, Instant.now().plusSeconds(3600)));

        AuthResponse response = authService.login("jefe@clubposeidon.com", "claveCorrecta");

        assertEquals("access-token-simulado", response.getAccessToken());
        assertEquals("refresh-token-simulado", response.getRefreshToken());
        verify(loginAttemptService).loginSucceeded("jefe@clubposeidon.com");
        verify(loginAttemptService, never()).loginFailed(any());
    }
}
