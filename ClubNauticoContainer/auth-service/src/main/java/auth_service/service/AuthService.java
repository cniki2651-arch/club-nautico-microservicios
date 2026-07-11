package auth_service.service;

import auth_service.entity.Usuario;
import auth_service.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Usuario register(Usuario usuario) {
        // Encriptamos la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public String login(String correo, String password) {
        // 1. Buscamos por correo, no por username
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        // 2. Verificamos la contraseña
        if (passwordEncoder.matches(password, usuario.getPassword())) {
            // 3. Pasamos el objeto usuario completo, no solo el nombre
            return jwtService.generateToken(usuario);
        } else {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }
    }   
}