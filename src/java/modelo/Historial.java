package modelo;

import java.sql.Timestamp;

/** Un cambio de estado de una incidencia (quién, cuándo y de qué estado a cuál). */
public class Historial {
    private Timestamp fechaCambio;
    private String estadoAnterior;   // null cuando es el registro inicial
    private String estadoNuevo;
    private String usuario;          // null si el cambio no tiene usuario asociado

    public Historial(Timestamp fechaCambio, String estadoAnterior, String estadoNuevo, String usuario) {
        this.fechaCambio = fechaCambio;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuario = usuario;
    }

    public Timestamp getFechaCambio() { return fechaCambio; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public String getUsuario() { return usuario; }
}
