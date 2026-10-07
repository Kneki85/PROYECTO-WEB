package modelo;

import java.sql.Date;

/**
 * Representa un empleado. Es un "POJO" (Plain Old Java Object):
 * solo guarda datos, no tiene lógica de base de datos.
 */
public class Empleado {
    private int idEmpleado;
    private String nombre;
    private String apellido;
    private String cargo;
    private String area;
    private Date fechaIngreso;

    public Empleado() {
    }

    public Empleado(int idEmpleado, String nombre, String apellido, String cargo, String area, Date fechaIngreso) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cargo = cargo;
        this.area = area;
        this.fechaIngreso = fechaIngreso;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Date getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Date fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return idEmpleado + " - " + getNombreCompleto() + " (" + cargo + ")";
    }
}
