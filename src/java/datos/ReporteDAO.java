package datos;

import modelo.ConteoTipo;
import modelo.EmpleadoIncidencias;
import modelo.Resumen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de resumen y reportes (agrupaciones con GROUP BY). */
public class ReporteDAO {

    /** Conteos de la pantalla de inicio: empleados y incidencias por estado. */
    public Resumen resumen() {
        String sql = "SELECT (SELECT COUNT(*) FROM empleado) AS empleados, "
                + "COALESCE(SUM(estado = 'Abierta'), 0) AS abiertas, "
                + "COALESCE(SUM(estado = 'En proceso'), 0) AS en_proceso, "
                + "COALESCE(SUM(estado = 'Cerrada'), 0) AS cerradas "
                + "FROM incidencia";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new Resumen(rs.getInt("empleados"), rs.getInt("abiertas"),
                    rs.getInt("en_proceso"), rs.getInt("cerradas"));
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al calcular el resumen", e);
        }
    }

    /** Cantidad de incidencias por tipo (incluye los tipos que aún no tienen ninguna). */
    public List<ConteoTipo> incidenciasPorTipo() {
        String sql = "SELECT t.nombre_tipo, COUNT(i.id_incidencia) AS total "
                + "FROM tipo_incidencia t "
                + "LEFT JOIN incidencia i ON i.id_tipo = t.id_tipo "
                + "GROUP BY t.id_tipo, t.nombre_tipo "
                + "ORDER BY total DESC, t.nombre_tipo";
        List<ConteoTipo> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new ConteoTipo(rs.getString("nombre_tipo"), rs.getInt("total")));
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al generar el reporte por tipo", e);
        }
        return lista;
    }

    /** Los empleados con más incidencias, con el total y cuántas siguen abiertas. */
    public List<EmpleadoIncidencias> empleadosConMasIncidencias(int limite) {
        String sql = "SELECT e.id_empleado, CONCAT(e.nombre, ' ', e.apellido) AS nombre, "
                + "COUNT(i.id_incidencia) AS total, "
                + "COALESCE(SUM(i.estado = 'Abierta'), 0) AS abiertas "
                + "FROM empleado e "
                + "JOIN incidencia i ON i.id_empleado = e.id_empleado "
                + "GROUP BY e.id_empleado, e.nombre, e.apellido "
                + "ORDER BY total DESC, abiertas DESC, e.apellido "
                + "LIMIT ?";
        List<EmpleadoIncidencias> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EmpleadoIncidencias(rs.getInt("id_empleado"), rs.getString("nombre"),
                            rs.getInt("total"), rs.getInt("abiertas")));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al generar el reporte por empleado", e);
        }
        return lista;
    }
}
