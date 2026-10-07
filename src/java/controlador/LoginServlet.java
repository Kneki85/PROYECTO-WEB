package controlador;

import datos.UsuarioDAO;
import modelo.Rol;
import modelo.Usuario;
import seguridad.Contrasenas;
import seguridad.LimitadorIntentos;
import seguridad.Seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/** Inicio y cierre de sesión. Es la única página a la que se puede entrar sin haber iniciado sesión. */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VISTA = "/WEB-INF/vistas/login.jsp";
    private static final String INICIO = "InicioServlet";
    private static final String SELECCION = "SeleccionServlet";
    private static final String MSG_INCORRECTO = "Usuario o contraseña incorrectos. Verifica e intenta de nuevo.";

    private final UsuarioDAO dao = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (Seguridad.usuarioActual(request) != null) {
            response.sendRedirect(request.getContextPath() + "/" + INICIO);
            return;
        }
        // El login se abre siempre con un tipo de personal elegido en la pantalla de selección
        Rol tipo = rolDeTipo(request.getParameter("tipo"));
        if (tipo == null) {
            response.sendRedirect(request.getContextPath() + "/" + SELECCION);
            return;
        }
        request.setAttribute("tipoRol", tipo);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /** Convierte el tipo elegido en la pantalla de selección ("admin" o "consulta") en un rol; null si no es válido. */
    private static Rol rolDeTipo(String tipo) {
        if ("admin".equals(tipo)) return Rol.ADMIN;
        if ("consulta".equals(tipo)) return Rol.CONSULTA;
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if ("salir".equals(request.getParameter("accion"))) {
            cerrarSesion(request, response);
            return;
        }
        iniciarSesion(request, response);
    }

    private void iniciarSesion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Rol tipo = rolDeTipo(request.getParameter("tipo"));
        if (tipo == null) {
            response.sendRedirect(request.getContextPath() + "/" + SELECCION);
            return;
        }
        request.setAttribute("tipoRol", tipo);
        String username = Peticion.valor(request.getParameter("usuario"));
        String clave = request.getParameter("clave");
        if (clave == null) clave = "";

        if (username.isEmpty() || clave.isEmpty()) {
            mostrarError(request, response, "Ingresa tu usuario y tu contraseña.", username);
            return;
        }
        // Límites para no gastar tiempo de CPU calculando hashes de textos gigantes
        if (username.length() > 50 || clave.length() > Contrasenas.MAX_LONGITUD) {
            mostrarError(request, response, MSG_INCORRECTO, username);
            return;
        }

        String claveLimite = username.toLowerCase(Locale.ROOT) + "|" + request.getRemoteAddr();
        long espera = LimitadorIntentos.segundosDeBloqueo(claveLimite);
        if (espera > 0) {
            long minutos = (espera + 59) / 60;
            mostrarError(request, response, "Demasiados intentos fallidos. Espera " + minutos
                    + (minutos == 1 ? " minuto" : " minutos") + " e intenta de nuevo.", username);
            return;
        }

        Usuario usuario = dao.buscarPorUsername(username);
        boolean correcto;
        if (usuario != null && usuario.isActivo()) {
            correcto = Contrasenas.verificar(clave, usuario.getPasswordHash());
        } else {
            Contrasenas.verificarFalso(clave); // mismo tiempo de respuesta exista o no el usuario
            correcto = false;
        }

        if (!correcto) {
            LimitadorIntentos.registrarFallo(claveLimite);
            LOG.warning("Intento de inicio de sesión fallido para el usuario '" + username
                    + "' desde " + request.getRemoteAddr());
            mostrarError(request, response, MSG_INCORRECTO, username);
            return;
        }

        // La contraseña es correcta, pero la cuenta debe corresponder al tipo de personal elegido.
        // El rol que manda es el guardado en la base de datos, nunca el que llega desde el navegador.
        if (usuario.getRol() != tipo) {
            LOG.warning("Ingreso rechazado: '" + username + "' (" + usuario.getRol() + ") intentó entrar como " + tipo);
            mostrarError(request, response, "Esta cuenta no pertenece a \"" + tipo.getEtiqueta()
                    + "\". Vuelve a la pantalla inicial y elige el tipo de personal que te corresponde.", username);
            return;
        }

        LimitadorIntentos.limpiar(claveLimite);

        // Cambiar el identificador de sesión al entrar evita el ataque de "fijación de sesión"
        HttpSession sesion = request.getSession(true);
        request.changeSessionId();
        usuario.setPasswordHash(null); // el hash no debe quedarse guardado en la sesión
        sesion.setAttribute(Seguridad.ATTR_USUARIO, usuario);
        sesion.setMaxInactiveInterval(Seguridad.MINUTOS_SESION * 60);
        Seguridad.renovarToken(sesion);

        LOG.info("Inicio de sesión: " + usuario.getUsername());
        response.sendRedirect(request.getContextPath() + "/" + INICIO);
    }

    private void cerrarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) sesion.invalidate();
        Mensajes.ok(response, request.getContextPath() + "/" + SELECCION, "Cerraste sesión correctamente.");
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, String mensaje,
                              String username) throws ServletException, IOException {
        request.setAttribute("errorLogin", mensaje);
        request.setAttribute("usuarioIngresado", username);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
