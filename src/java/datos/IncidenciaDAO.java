package datos;

import modelo.FiltroIncidencias;
import modelo.Incidencia;
import modelo.Pagina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IncidenciaDAO {

    private static final String ORDEN = " ORDER BY i.fecha_registro DESC, i.id_incidencia DESC";

    private static final String SELECT_BASE =
            "SELECT i.id_incidencia, i.id_empleado, i.id_tipo, i.descripcion, i.fecha_registro, i.estado, "
            + "CONCAT(e.nombre, ' ', e.apellido) AS empleado_nombre, t.nombre_tipo "
            + "FROM incidencia i "
            + "JOIN empleado e ON e.id_empleado = i.id_empleado "
            + "JOIN tipo_incidencia t ON t.id_tipo = i.id_tipo";

    private static final String FROM_BASE =
            " FROM incidencia i "
            + "JOIN empleado e ON e.id_empleado = i.id_empleado "
            + "JOIN tipo_incidencia t ON t.id_tipo = i.id_tipo";

    private final HistorialDAO historialDAO = new HistorialDAO();

    /** Una operación que se ejecuta dentro de una transacción. */
    private interface Operacion {
        boolean ejecutar(Connection con) throws SQLException;
    }

    /**
     * Ejecuta la operación en una transacción: si algo falla se deshace todo (rollback),
     * así nunca queda una incidencia modificada sin su historial (ni al revés).
     */
    private boolean enTransaccion(Operacion operacion, String contexto) {
        try (Connection con = Conexion.conectar()) {
            con.setAutoCommit(false);
            try {
                boolean resultado = operacion.ejecutar(con);
                if (resultado) con.commit(); else con.rollback();
                return resultado;
            } catch (SQLIntegrityConstraintViolationException e) {
                con.rollback();
                return false; // por ejemplo: el empleado o el tipo ya no existen
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al " + contexto, e);
        }
    }

    /** Registra una incidencia nueva y deja constancia de su estado inicial en el historial. */
    public boolean registrar(Incidencia inc, Integer idUsuario) {
        return enTransaccion(con -> {
            String sql = "INSERT INTO incidencia (id_empleado, id_tipo, descripcion, fecha_registro, estado) "
                    + "VALUES (?, ?, ?, ?, ?)";
            int idNuevo;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, inc.getIdEmpleado());
                ps.setInt(2, inc.getIdTipo());
                ps.setString(3, inc.getDescripcion());
                ps.setDate(4, inc.getFechaRegistro());
                ps.setString(5, inc.getEstado());
                if (ps.executeUpdate() == 0) return false;
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (!claves.next()) return false;
                    idNuevo = claves.getInt(1);
                }
            }
            historialDAO.registrarCambio(con, idNuevo, null, inc.getEstado(), idUsuario);
            return true;
        }, "registrar la incidencia");
    }

    /** Edita todos los datos de una incidencia; si el estado cambió, lo anota en el historial. */
    public boolean actualizar(Incidencia inc, Integer idUsuario) {
        return enTransaccion(con -> {
            String anterior = estadoBloqueado(con, inc.getIdIncidencia());
            if (anterior == null) return false; // ya no existe
            String sql = "UPDATE incidencia SET id_empleado=?, id_tipo=?, descripcion=?, "
                    + "fecha_registro=?, estado=? WHERE id_incidencia=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, inc.getIdEmpleado());
                ps.setInt(2, inc.getIdTipo());
                ps.setString(3, inc.getDescripcion());
                ps.setDate(4, inc.getFechaRegistro());
                ps.setString(5, inc.getEstado());
                ps.setInt(6, inc.getIdIncidencia());
                ps.executeUpdate();
            }
            if (!anterior.equals(inc.getEstado())) {
                historialDAO.registrarCambio(con, inc.getIdIncidencia(), anterior, inc.getEstado(), idUsuario);
            }
            return true;
        }, "actualizar la incidencia");
    }

    /** Lee el estado actual bloqueando la fila hasta terminar la transacción (evita cambios simultáneos). */
    private String estadoBloqueado(Connection con, int idIncidencia) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT estado FROM incidencia WHERE id_incidencia=? FOR UPDATE")) {
            ps.setInt(1, idIncidencia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("estado") : null;
            }
        }
    }

    public boolean eliminar(int idIncidencia) {
        String sql = "DELETE FROM incidencia WHERE id_incidencia=?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIncidencia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al eliminar la incidencia", e);
        }
    }

    public Incidencia buscarPorId(int idIncidencia) {
        String sql = SELECT_BASE + " WHERE i.id_incidencia = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIncidencia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al buscar la incidencia", e);
        }
    }

    /** Búsqueda con filtros y paginación (para el listado en pantalla). */
    public Pagina<Incidencia> buscar(FiltroIncidencias filtro, int pagina, int tamano) {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(filtro, params);

        try (Connection con = Conexion.conectar()) {
            int total;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*)" + FROM_BASE + where)) {
                asignar(ps, params, 1);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getInt(1);
                }
            }

            int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) tamano));
            int paginaValida = Math.min(Math.max(1, pagina), totalPaginas);

            List<Incidencia> items = new ArrayList<>();
            String sql = SELECT_BASE + where + ORDEN + " LIMIT ? OFFSET ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int siguiente = asignar(ps, params, 1);
                ps.setInt(siguiente, tamano);
                ps.setInt(siguiente + 1, (paginaValida - 1) * tamano);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(mapear(rs));
                }
            }
            return new Pagina<>(items, total, paginaValida, tamano);
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al buscar incidencias", e);
        }
    }

    /** Todas las incidencias que cumplen el filtro, sin paginar (para exportar a CSV). */
    public List<Incidencia> buscarTodas(FiltroIncidencias filtro) {
        List<Object> params = new ArrayList<>();
        String sql = SELECT_BASE + construirWhere(filtro, params) + ORDEN;
        List<Incidencia> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignar(ps, params, 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al listar incidencias", e);
        }
        return lista;
    }

    /** Arma el WHERE según los criterios usados. Todo va como parámetro (?), nunca concatenado. */
    private String construirWhere(FiltroIncidencias f, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (!f.getTexto().isEmpty()) {
            where.append(" AND (i.descripcion LIKE ? ESCAPE '!' OR CONCAT(e.nombre, ' ', e.apellido) LIKE ? ESCAPE '!')");
            String patron = "%" + escaparLike(f.getTexto()) + "%";
            params.add(patron);
            params.add(patron);
        }
        if (!f.getEstado().isEmpty()) {
            where.append(" AND i.estado = ?");
            params.add(f.getEstado());
        }
        if (f.getIdTipo() != null) {
            where.append(" AND i.id_tipo = ?");
            params.add(f.getIdTipo());
        }
        if (f.getDesde() != null) {
            where.append(" AND i.fecha_registro >= ?");
            params.add(java.sql.Date.valueOf(f.getDesde()));
        }
        if (f.getHasta() != null) {
            where.append(" AND i.fecha_registro <= ?");
            params.add(java.sql.Date.valueOf(f.getHasta()));
        }
        return where.toString();
    }

    /** Evita que un "%" o "_" escrito por el usuario se interprete como comodín de LIKE. */
    private String escaparLike(String texto) {
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    /** Asigna los parámetros desde la posición indicada y devuelve la siguiente posición libre. */
    private int asignar(PreparedStatement ps, List<Object> params, int desde) throws SQLException {
        int pos = desde;
        for (Object p : params) {
            ps.setObject(pos++, p);
        }
        return pos;
    }

    private Incidencia mapear(ResultSet rs) throws SQLException {
        Incidencia inc = new Incidencia();
        inc.setIdIncidencia(rs.getInt("id_incidencia"));
        inc.setIdEmpleado(rs.getInt("id_empleado"));
        inc.setIdTipo(rs.getInt("id_tipo"));
        inc.setDescripcion(rs.getString("descripcion"));
        inc.setFechaRegistro(rs.getDate("fecha_registro"));
        inc.setEstado(rs.getString("estado"));
        inc.setEmpleadoNombre(rs.getString("empleado_nombre"));
        inc.setTipoNombre(rs.getString("nombre_tipo"));
        return inc;
    }
}
