package datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Solo necesitamos leer el catálogo de tipos, no crearlos/editarlos
 * desde la aplicación (eso lo maneja el administrador directo en BD).
 */
public class TipoIncidenciaDAO {

    /** Retorna un mapa id -> nombre, útil para llenar un combo/select. */
    public Map<Integer, String> listarTodos() {
        Map<Integer, String> mapa = new LinkedHashMap<>();
        String sql = "SELECT id_tipo, nombre_tipo FROM tipo_incidencia ORDER BY id_tipo";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getInt("id_tipo"), rs.getString("nombre_tipo"));
            }
        } catch (SQLException ex) {
            throw new DatosException("Error en la base de datos al listar tipos de incidencia", ex);
        }
        return mapa;
    }
}
