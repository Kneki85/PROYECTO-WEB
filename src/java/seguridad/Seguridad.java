package seguridad;

import modelo.Rol;
import modelo.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Utilidades de sesión, permisos y protección CSRF que usan los servlets y los JSP. */
public final class Seguridad {

    public static final String ATTR_USUARIO = "usuario";
    public static final String ATTR_CSRF = "csrf_token";
    public static final String PARAM_CSRF = "_csrf";
    public static final int MINUTOS_SESION = 30;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Seguridad() {
    }

    /** Usuario con sesión iniciada, o null si no hay sesión. */
    public static Usuario usuarioActual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute(ATTR_USUARIO);
    }

    public static boolean esAdmin(HttpServletRequest request) {
        Usuario u = usuarioActual(request);
        return u != null && u.getRol() == Rol.ADMIN;
    }

    /** Si el usuario no es administrador responde 403 y devuelve false (el servlet debe detenerse). */
    public static boolean exigirAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (esAdmin(request)) return true;
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Se requiere rol de administrador");
        return false;
    }

    /**
     * Token CSRF de la sesión. Cada formulario POST lo incluye y el filtro lo verifica: así otro
     * sitio web no puede hacer que el navegador del usuario envíe acciones sin que este lo sepa.
     */
    public static synchronized String token(HttpSession sesion) {
        String token = (String) sesion.getAttribute(ATTR_CSRF);
        if (token == null) {
            token = nuevoToken();
            sesion.setAttribute(ATTR_CSRF, token);
        }
        return token;
    }

    public static synchronized void renovarToken(HttpSession sesion) {
        sesion.setAttribute(ATTR_CSRF, nuevoToken());
    }

    /** Campo oculto listo para pegar dentro de cualquier formulario POST. */
    public static String campoCsrf(HttpServletRequest request) {
        return "<input type=\"hidden\" name=\"" + PARAM_CSRF + "\" value=\""
                + token(request.getSession()) + "\">";
    }

    public static boolean tokenValido(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) return false;
        String esperado = (String) sesion.getAttribute(ATTR_CSRF);
        String recibido = request.getParameter(PARAM_CSRF);
        if (esperado == null || recibido == null) return false;
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
