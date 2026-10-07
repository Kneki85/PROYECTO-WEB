package seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Manejo seguro de contraseñas con PBKDF2-HMAC-SHA256 (incluido en Java, sin librerías externas).
 *
 * Nunca se guarda la contraseña: se guarda un texto con este formato
 *     pbkdf2_sha256$ITERACIONES$SAL_BASE64$HASH_BASE64
 * La sal es aleatoria y distinta por usuario (dos personas con la misma contraseña tienen hashes
 * distintos) y las iteraciones hacen lento probar contraseñas por fuerza bruta. Como las
 * iteraciones viajan dentro del texto, se pueden subir en el futuro sin invalidar los hashes viejos.
 */
public final class Contrasenas {

    public static final int MIN_LONGITUD = 8;
    public static final int MAX_LONGITUD = 128;

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int ITERACIONES = 600_000;   // recomendación OWASP para PBKDF2-HMAC-SHA256
    private static final int LONGITUD_SAL = 16;       // bytes
    private static final int LONGITUD_HASH = 256;     // bits
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final byte[] SAL_FALSA = new byte[LONGITUD_SAL];

    private Contrasenas() {
    }

    /** Genera el texto que se guarda en la base de datos para una contraseña. */
    public static String generarHash(String clave) {
        byte[] sal = new byte[LONGITUD_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Comprueba una contraseña contra el texto guardado. Devuelve false ante cualquier formato inválido. */
    public static boolean verificar(String clave, String almacenado) {
        if (clave == null || almacenado == null) return false;
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            if (iteraciones < 10_000 || iteraciones > 5_000_000) return false;
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(clave, sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado); // comparación en tiempo constante
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Hace el mismo trabajo que una verificación real. Se usa cuando el usuario no existe,
     * para que responder "usuario inexistente" no sea más rápido que "contraseña incorrecta"
     * (así no se puede averiguar qué usuarios existen midiendo el tiempo de respuesta).
     */
    public static void verificarFalso(String clave) {
        derivar(clave == null ? "" : clave, SAL_FALSA, ITERACIONES);
    }

    /** Valida la política de contraseñas. Devuelve el mensaje del problema o null si es válida. */
    public static String validarPolitica(String clave, String username) {
        if (clave == null || clave.length() < MIN_LONGITUD) {
            return "La contraseña debe tener al menos " + MIN_LONGITUD + " caracteres.";
        }
        if (clave.length() > MAX_LONGITUD) {
            return "La contraseña no puede superar los " + MAX_LONGITUD + " caracteres.";
        }
        boolean hayLetra = false;
        boolean hayNumero = false;
        for (char c : clave.toCharArray()) {
            if (Character.isLetter(c)) hayLetra = true;
            if (Character.isDigit(c)) hayNumero = true;
        }
        if (!hayLetra || !hayNumero) {
            return "La contraseña debe combinar letras y números.";
        }
        if (username != null && clave.equalsIgnoreCase(username)) {
            return "La contraseña no puede ser igual al nombre de usuario.";
        }
        return null;
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, LONGITUD_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }
}
