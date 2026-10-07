package seguridad;

import modelo.Usuario;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Puerta de entrada de TODA la aplicación:
 *   1. Añade cabeceras de seguridad a las respuestas.
 *   2. Deja pasar sin sesión solo la pantalla de selección de tipo de personal, el login y los estilos (css).
 *   3. Redirige a la selección de tipo de personal a quien no haya iniciado sesión.
 *      Quien tenga una contraseña temporal solo puede llegar a "Mi cuenta" hasta cambiarla.
 *   4. Exige el token CSRF en todas las peticiones POST.
 */
@WebFilter("/*")
public class FiltroSeguridad implements Filter {

    private static final String LOGIN = "/LoginServlet";
    private static final String SELECCION = "/SeleccionServlet";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String ruta = request.getServletPath();

        // Los estilos son públicos (el login también los necesita)
        if (ruta.startsWith("/css/")) {
            cadena.doFilter(req, res);
            return;
        }

        // Debe hacerse ANTES de leer cualquier parámetro (el token CSRF también es un parámetro);
        // si no, las tildes y la ñ de los formularios pueden guardarse dañadas.
        request.setCharacterEncoding("UTF-8");

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        // No guardar páginas con datos en la caché: tras cerrar sesión, "Atrás" no debe mostrarlas
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        boolean esLogin = LOGIN.equals(ruta);
        boolean esPublica = esLogin || SELECCION.equals(ruta);

        if (!esPublica && Seguridad.usuarioActual(request) == null) {
            response.sendRedirect(request.getContextPath() + SELECCION);
            return;
        }

        // Con una contraseña temporal solo se puede usar "Mi cuenta" (para cambiarla) o cerrar sesión
        Usuario usuario = Seguridad.usuarioActual(request);
        if (usuario != null && usuario.isDebeCambiarClave() && !esPublica && !"/CuentaServlet".equals(ruta)) {
            String mensaje = URLEncoder.encode("Debes cambiar tu contraseña temporal antes de continuar.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/CuentaServlet?error=" + mensaje);
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod()) && !Seguridad.tokenValido(request)) {
            if (esLogin) {
                String mensaje = URLEncoder.encode("La página caducó. Vuelve a intentarlo.", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + SELECCION + "?error=" + mensaje);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida o sesión caducada");
            }
            return;
        }

        cadena.doFilter(req, res);
    }
}
