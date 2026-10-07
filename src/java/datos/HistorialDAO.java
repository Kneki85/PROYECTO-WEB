package datos;

import modelo.Historial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class HistorialDAO {

    /**
     * Registra un cambio de estado usando la conexión (y por tanto la transacción) que recibe,
     * para que el cambio y su historial se guarden juntos o no se guarden.
     * estadoAnterior es null cuando se trata del registro inicial de la incidencia.
     * idUsuario puede ser null si no se conoce quién hizo el cambio.
     */
    public void registrarCambio(Connection con, int idIncidencia, String estadoAnterior,
                                String estadoNuevo, Integer idUsuario) throws SQLException {
        String sql = "INSERT INTO historial_incidencia (id_incidencia, estado_anterior, estado_nuevo, id_usuario) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIncidencia);
            if (estadoAnterior == null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, estadoAnterior);
            ps.setString(3, estadoNuevo);
            if (idUsuario == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, idUsuario);
            ps.executeUpdate();
        }
    }

    /** Historial completo de una incidencia, del más antiguo al más reciente. */
    public List<Historial> listarPorIncidencia(int idIncidencia) {
        List<Historial> lista = new ArrayList<>();
        String sql = "SELECT h.estado_anterior, h.estado_nuevo, h.fecha_cambio, u.nombre_completo "
                + "FROM historial_incidencia h "
                + "LEFT JOIN usuario u ON u.id_usuario = h.id_usuario "
                + "WHERE h.id_incidencia = ? ORDER BY h.fecha_cambio, h.id_historial";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIncidencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Historial(
                            rs.getTimestamp("fecha_cambio"),
                            rs.getString("estado_anterior"),
                            rs.getString("estado_nuevo"),
                            rs.getString("nombre_completo")));
                }
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al listar el historial", e);
        }
        return lista;
    }
}
