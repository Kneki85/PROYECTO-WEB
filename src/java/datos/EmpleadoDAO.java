package datos;

import modelo.Empleado;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public boolean registrar(Empleado e) {
        String sql = "INSERT INTO empleado (nombre, apellido, cargo, area, fecha_ingreso) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getCargo());
            ps.setString(4, e.getArea());
            ps.setDate(5, e.getFechaIngreso() != null ? e.getFechaIngreso() : Date.valueOf(LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatosException("Error en la base de datos al registrar el empleado", ex);
        }
    }

    public boolean actualizar(Empleado e) {
        String sql = "UPDATE empleado SET nombre=?, apellido=?, cargo=?, area=?, fecha_ingreso=? WHERE id_empleado=?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getCargo());
            ps.setString(4, e.getArea());
            ps.setDate(5, e.getFechaIngreso());
            ps.setInt(6, e.getIdEmpleado());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatosException("Error en la base de datos al actualizar el empleado", ex);
        }
    }

    /** Elimina al empleado y, por la llave foránea (ON DELETE CASCADE), también sus incidencias. */
    public boolean eliminar(int idEmpleado) {
        String sql = "DELETE FROM empleado WHERE id_empleado=?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatosException("Error en la base de datos al eliminar el empleado", ex);
        }
    }

    public Empleado buscarPorId(int idEmpleado) {
        String sql = "SELECT * FROM empleado WHERE id_empleado=?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DatosException("Error en la base de datos al buscar el empleado", ex);
        }
    }

    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleado ORDER BY apellido, nombre";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            throw new DatosException("Error en la base de datos al listar empleados", ex);
        }
        return lista;
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("id_empleado"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("cargo"),
                rs.getString("area"),
                rs.getDate("fecha_ingreso"));
    }
}
