package datos;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

/**
 * Clase encargada únicamente de entregar conexiones a la base de datos.
 * Todas las clases DAO la usan para hablar con MySQL.
 *
 * Usa un POOL de conexiones (el que ya trae Tomcat, tomcat-dbcp): las conexiones se abren una
 * vez y se reutilizan. Antes cada consulta abría una conexión nueva, y con una base en la nube
 * (conexión cifrada + latencia de red) eso hacía lento cada cambio de pantalla.
 * Los DAO no cambian: al cerrar la conexión (try-with-resources) esta vuelve al pool.
 *
 * Por defecto usa los datos típicos de XAMPP. Para cambiarlos sin tocar el código
 * (por ejemplo, en la nube) define estas variables de entorno o propiedades
 * de la JVM: INCIDENCIAS_DB_URL, INCIDENCIAS_DB_USER, INCIDENCIAS_DB_PASSWORD.
 */
public class Conexion {

    private static final String URL = config("INCIDENCIAS_DB_URL", "jdbc:mysql://localhost:3306/incidencias_db");
    private static final String USUARIO = config("INCIDENCIAS_DB_USER", "root");
    private static final String PASSWORD = config("INCIDENCIAS_DB_PASSWORD", ""); // XAMPP: sin contraseña

    private static final BasicDataSource POOL = crearPool();

    private static BasicDataSource crearPool() {
        BasicDataSource ds = new BasicDataSource();
        // El driver vive en WEB-INF/lib de la aplicación y el pool en las librerías de Tomcat:
        // hay que indicarle al pool con qué cargador de clases debe buscar el driver.
        ds.setDriverClassLoader(Conexion.class.getClassLoader());
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(URL);
        ds.setUsername(USUARIO);
        ds.setPassword(PASSWORD);
        ds.setMaxTotal(5);                 // máximo de conexiones abiertas a la vez (la base gratis tiene tope)
        ds.setMaxIdle(3);                  // conexiones que se dejan abiertas esperando
        ds.setMaxWaitMillis(10_000);       // si todas están ocupadas, espera hasta 10 s y luego falla
        ds.setTestOnBorrow(true);          // comprueba que la conexión sigue viva antes de usarla
        ds.setValidationQueryTimeout(3);   // (si la base cortó una conexión inactiva, se abre otra)
        return ds;
    }

    private static String config(String clave, String porDefecto) {
        String valor = System.getenv(clave);
        if (valor == null) valor = System.getProperty(clave);
        return valor == null ? porDefecto : valor;
    }

    public static Connection conectar() {
        try {
            return POOL.getConnection();
        } catch (SQLException e) {
            throw new DatosException("Error al conectar con la base de datos: " + e.getMessage(), e);
        }
    }
}
