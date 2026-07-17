package auth_service.dto;

public class EditarUsuarioRequest {
    private String nombres;
    private String apellidos;
    private Integer id_rol;

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public Integer getIdRol() { return id_rol; }
    public void setIdRol(Integer id_rol) { this.id_rol = id_rol; }
}
