package modelo;

import java.sql.Timestamp;

/** Usuario que puede iniciar sesión en el sistema. */
public class Usuario {
    private int idUsuario;
    private String username;
    private String nombreCompleto;
    private String passwordHash;
    private Rol rol;
    private boolean activo;
    private boolean debeCambiarClave;
    private Timestamp fechaCreacion;

    public Usuario() {
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** true si la contraseña es temporal (recién creada o restablecida) y debe cambiarse al entrar. */
    public boolean isDebeCambiarClave() { return debeCambiarClave; }
    public void setDebeCambiarClave(boolean debeCambiarClave) { this.debeCambiarClave = debeCambiarClave; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
