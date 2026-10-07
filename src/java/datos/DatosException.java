package datos;

/**
 * Error al hablar con la base de datos. Es una excepción "sin verificar" para que los DAO
 * no obliguen a cada método a declarar throws; la página error.jsp muestra un mensaje amigable.
 * El texto siempre contiene "base de datos" (error.jsp lo usa para reconocer este tipo de fallo).
 */
public class DatosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public DatosException(String mensaje) {
        super(mensaje);
    }
}
