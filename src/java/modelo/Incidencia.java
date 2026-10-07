package modelo;

import java.sql.Date;

public class Incidencia {
    private int idIncidencia;
    private int idEmpleado;
    private int idTipo;
    private String descripcion;
    private Date fechaRegistro;
    private String estado;

    // Datos de solo lectura que llegan de los JOIN (para mostrar en pantalla)
    private String empleadoNombre;
    private String tipoNombre;

    public Incidencia() {
    }

    public Incidencia(int idEmpleado, int idTipo, String descripcion, Date fechaRegistro, String estado) {
        this.idEmpleado = idEmpleado;
        this.idTipo = idTipo;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
    }

    public int getIdIncidencia() { return idIncidencia; }
    public void setIdIncidencia(int idIncidencia) { this.idIncidencia = idIncidencia; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { this.idTipo = idTipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEmpleadoNombre() { return empleadoNombre; }
    public void setEmpleadoNombre(String empleadoNombre) { this.empleadoNombre = empleadoNombre; }

    public String getTipoNombre() { return tipoNombre; }
    public void setTipoNombre(String tipoNombre) { this.tipoNombre = tipoNombre; }

    @Override
    public String toString() {
        return idIncidencia + " | Empleado " + idEmpleado + " | Tipo " + idTipo
                + " | " + descripcion + " | " + fechaRegistro + " | " + estado;
    }
}
