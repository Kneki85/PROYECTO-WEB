package datos;

import modelo.Rol;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario buscarPorUsername(String username) {
        return buscarUno("SELECT * FROM usuario WHERE username = ?", username);
    }

    public Usuario buscarPorId(int idUsuario) {
        return buscarUno("SELECT * FROM usuario WHERE id_usuario = ?", idUsuario);
    }

    private Usuario buscarUno(String sql, Object parametro) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al buscar el usuario", e);
        }
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY username";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al listar usuarios", e);
        }
        return lista;
    }

    /** Crea un usuario. Devuelve false si el nombre de usuario ya existe. */
    public boolean crear(Usuario u) {
        String sql = "INSERT INTO usuario (username, nombre_completo, password_hash, rol, activo, debe_cambiar_clave) VALUES (?, ?, ?, ?, 1, 1)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getNombreCompleto());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRol().name());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al crear el usuario", e);
        }
    }

    /**
     * Guarda una nueva contraseña. Si es temporal (la puso un administrador) se marca para que la
     * persona deba cambiarla en su próximo ingreso; si la eligió ella misma, se quita la marca.
     */
    public boolean cambiarClave(int idUsuario, String nuevoHash, boolean esTemporal) {
        String sql = "UPDATE usuario SET password_hash = ?, debe_cambiar_clave = ? WHERE id_usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, esTemporal ? 1 : 0);
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al cambiar la contraseña", e);
        }
    }

    public boolean cambiarActivo(int idUsuario, boolean activo) {
        return actualizar("UPDATE usuario SET activo = ? WHERE id_usuario = ?", activo ? 1 : 0, idUsuario);
    }

    private boolean actualizar(String sql, Object valor, int idUsuario) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, valor);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatosException("Error en la base de datos al actualizar el usuario", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setPasswordHash(rs.getString("password_hash"));
        Rol rol = Rol.desde(rs.getString("rol"));
        u.setRol(rol != null ? rol : Rol.CONSULTA); // ante un valor desconocido, el rol con menos permisos
        u.setActivo(rs.getInt("activo") == 1);
        u.setDebeCambiarClave(rs.getInt("debe_cambiar_clave") == 1);
        u.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        return u;
    }
}
