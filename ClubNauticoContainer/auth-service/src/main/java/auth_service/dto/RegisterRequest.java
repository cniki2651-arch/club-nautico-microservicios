package auth_service.dto;

public class RegisterRequest {
    private String correo;
    private String password;
    private String nombres;
    private String apellidos;
    private String rol;

    public RegisterRequest() {}

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
