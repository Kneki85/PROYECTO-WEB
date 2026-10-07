package controlador;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utilidad para mostrar mensajes al usuario (éxito / error) después de una acción.
 * Redirige a la pantalla indicada agregando ?ok=... o ?error=... a la URL,
 * y la cabecera común de las páginas lee esos parámetros para mostrar el aviso.
 */
public final class Mensajes {

    private Mensajes() {
    }

    public static void ok(HttpServletResponse response, String destino, String mensaje) throws IOException {
        redirigir(response, destino, "ok", mensaje);
    }

    public static void error(HttpServletResponse response, String destino, String mensaje) throws IOException {
        redirigir(response, destino, "error", mensaje);
    }

    private static void redirigir(HttpServletResponse response, String destino, String clave, String mensaje)
            throws IOException {
        String separador = destino.contains("?") ? "&" : "?";
        response.sendRedirect(destino + separador + clave + "="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
    }

    /** Escapa caracteres especiales de HTML para mostrar texto de forma segura en las páginas. */
    public static String escapar(String texto) {
        return Vista.escapar(texto);
    }
}
