package auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String testAcceso() {
        return "¡Éxito! Has accedido a una ruta protegida con tu Token JWT.";
    }
}