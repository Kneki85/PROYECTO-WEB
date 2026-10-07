package datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada únicamente de abrir la conexión a la base de datos.
 * Todas las clases DAO la usan para hablar con MySQL.
 *
 * Por defecto usa los datos típicos de XAMPP. Para cambiarlos sin tocar el código
 * (por ejemplo, si root tiene contraseña) define estas variables de entorno o propiedades
 * de la JVM: INCIDENCIAS_DB_URL, INCIDENCIAS_DB_USER, INCIDENCIAS_DB_PASSWORD.
 */
public class Conexion {

    private static final String URL = config("INCIDENCIAS_DB_URL", "jdbc:mysql://localhost:3306/incidencias_db");
    private static final String USUARIO = config("INCIDENCIAS_DB_USER", "root");
    private static final String PASSWORD = config("INCIDENCIAS_DB_PASSWORD", ""); // XAMPP: sin contraseña

    static {
        // Registro explícito del driver. En algunos Tomcat el auto-registro vía META-INF/services
        // no "ve" el jar de WEB-INF/lib y sale "No suitable driver found" aunque el .jar esté.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatosException("No se encontró el driver de la base de datos (mysql-connector-j) en WEB-INF/lib", e);
        }
    }

    private static String config(String clave, String porDefecto) {
        String valor = System.getenv(clave);
        if (valor == null) valor = System.getProperty(clave);
        return valor == null ? porDefecto : valor;
    }

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (SQLException e) {
            throw new DatosException("Error al conectar con la base de datos: " + e.getMessage(), e);
        }
    }
}
