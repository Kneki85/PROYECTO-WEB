package controlador;

import seguridad.Seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Ventana inicial del sistema: el usuario elige su tipo de personal (Administrador de personal o
 * Personal de consulta) y desde ahí pasa al inicio de sesión correspondiente.
 * Es una de las dos páginas a las que se puede entrar sin haber iniciado sesión (la otra es el login).
 */
@WebServlet("/SeleccionServlet")
public class SeleccionServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/seleccion.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Quien ya inició sesión no necesita volver a elegir: va directo al inicio
        if (Seguridad.usuarioActual(request) != null) {
            response.sendRedirect(request.getContextPath() + "/InicioServlet");
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
