package controlador;

import datos.UsuarioDAO;
import modelo.Usuario;
import seguridad.Contrasenas;
import seguridad.Seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/** "Mi cuenta": cada usuario (cualquier rol) puede cambiar su propia contraseña. */
@WebServlet("/CuentaServlet")
public class CuentaServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CuentaServlet.class.getName());
    private static final String VISTA = "/WEB-INF/vistas/cuenta.jsp";

    private final UsuarioDAO dao = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Usuario sesion = Seguridad.usuarioActual(request);
        Usuario actual = dao.buscarPorId(sesion.getIdUsuario());

        String claveActual = request.getParameter("claveActual");
        String nueva = request.getParameter("clave");
        String confirmacion = request.getParameter("confirmacion");

        String problema;
        if (actual == null || !actual.isActivo()) {
            problema = "Tu usuario ya no está disponible.";
        } else if (claveActual == null || !Contrasenas.verificar(claveActual, actual.getPasswordHash())) {
            problema = "La contraseña actual no es correcta.";
        } else if (nueva == null || !nueva.equals(confirmacion)) {
            problema = "Las contraseñas nuevas no coinciden.";
        } else if (nueva.equals(claveActual)) {
            problema = "La contraseña nueva debe ser distinta de la actual.";
        } else {
            problema = Contrasenas.validarPolitica(nueva, actual.getUsername());
        }

        if (problema != null) {
            request.setAttribute("error", problema);
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        dao.cambiarClave(actual.getIdUsuario(), Contrasenas.generarHash(nueva), false);
        sesion.setDebeCambiarClave(false);
        request.changeSessionId(); // buena práctica: nuevo identificador de sesión tras cambiar la contraseña
        Seguridad.renovarToken(request.getSession());
        LOG.info("El usuario '" + actual.getUsername() + "' cambió su contraseña");
        Mensajes.ok(response, "CuentaServlet", "Tu contraseña se cambió correctamente.");
    }
}
