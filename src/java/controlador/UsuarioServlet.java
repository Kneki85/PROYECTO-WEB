package controlador;

import datos.DatosException;
import datos.UsuarioDAO;
import modelo.Rol;
import modelo.Usuario;
import seguridad.Contrasenas;
import seguridad.Seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Administración de usuarios (solo administradores): crear, activar/desactivar y restablecer contraseña. */
@WebServlet("/UsuarioServlet")
public class UsuarioServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UsuarioServlet.class.getName());
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
    private static final String PAGINA = "UsuarioServlet";
    private static final String VISTA = "/WEB-INF/vistas/usuarios/listar.jsp";

    private final UsuarioDAO dao = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Seguridad.exigirAdmin(request, response)) return;

        if ("clave".equals(request.getParameter("accion"))) {
            Integer id = Peticion.aEntero(request.getParameter("id"));
            Usuario destino = (id != null) ? dao.buscarPorId(id) : null;
            if (destino == null) {
                Mensajes.error(response, PAGINA, "El usuario indicado no existe.");
                return;
            }
            request.setAttribute("usuarioClave", destino);
        }
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Seguridad.exigirAdmin(request, response)) return;
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        switch (accion) {
            case "crear":
                crear(request, response);
                break;
            case "estado":
                cambiarEstado(request, response);
                break;
            case "clave":
                restablecerClave(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = Peticion.valor(request.getParameter("username"));
        String nombre = Peticion.valor(request.getParameter("nombreCompleto"));
        Rol rol = Rol.desde(request.getParameter("rol"));
        String clave = request.getParameter("clave");
        String confirmacion = request.getParameter("confirmacion");

        // Se conservan los datos escritos (menos la contraseña) para no obligar a repetirlos
        request.setAttribute("formUsername", username);
        request.setAttribute("formNombre", nombre);
        request.setAttribute("formRol", rol == null ? Rol.CONSULTA.name() : rol.name());

        String problema = null;
        if (!USERNAME.matcher(username).matches()) {
            problema = "El usuario debe tener de 3 a 50 caracteres: letras, números, punto, guion o guion bajo.";
        } else if (nombre.length() < 2 || nombre.length() > 120) {
            problema = "El nombre completo debe tener entre 2 y 120 caracteres.";
        } else if (rol == null) {
            problema = "Selecciona un rol válido.";
        } else if (clave == null || !clave.equals(confirmacion)) {
            problema = "Las contraseñas no coinciden.";
        } else {
            problema = Contrasenas.validarPolitica(clave, username);
        }
        if (problema != null) {
            request.setAttribute("error", problema);
            mostrar(request, response);
            return;
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setNombreCompleto(nombre);
        nuevo.setRol(rol);
        nuevo.setPasswordHash(Contrasenas.generarHash(clave));

        boolean creado;
        try {
            creado = dao.crear(nuevo);
        } catch (DatosException e) {
            LOG.log(Level.SEVERE, "No se pudo crear el usuario", e);
            request.setAttribute("error", "No se pudo crear el usuario. Intenta de nuevo.");
            mostrar(request, response);
            return;
        }
        if (!creado) {
            request.setAttribute("error", "Ese nombre de usuario ya existe.");
            mostrar(request, response);
            return;
        }
        LOG.info("Usuario '" + username + "' creado por " + Seguridad.usuarioActual(request).getUsername());
        Mensajes.ok(response, PAGINA, "Usuario creado correctamente.");
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = Peticion.aEntero(request.getParameter("id"));
        boolean activar = "true".equals(request.getParameter("activo"));
        if (id == null) {
            Mensajes.error(response, PAGINA, "Identificador de usuario inválido.");
            return;
        }
        if (!activar && id == Seguridad.usuarioActual(request).getIdUsuario()) {
            Mensajes.error(response, PAGINA, "No puedes desactivar tu propio usuario.");
            return;
        }
        if (dao.cambiarActivo(id, activar)) {
            LOG.info("Usuario " + id + (activar ? " activado" : " desactivado") + " por "
                    + Seguridad.usuarioActual(request).getUsername());
            Mensajes.ok(response, PAGINA, activar ? "Usuario activado." : "Usuario desactivado.");
        } else {
            Mensajes.error(response, PAGINA, "No se encontró el usuario.");
        }
    }

    private void restablecerClave(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = Peticion.aEntero(request.getParameter("id"));
        Usuario destino = (id != null) ? dao.buscarPorId(id) : null;
        if (destino == null) {
            Mensajes.error(response, PAGINA, "El usuario indicado no existe.");
            return;
        }
        String clave = request.getParameter("clave");
        String confirmacion = request.getParameter("confirmacion");

        String problema;
        if (clave == null || !clave.equals(confirmacion)) {
            problema = "Las contraseñas no coinciden.";
        } else {
            problema = Contrasenas.validarPolitica(clave, destino.getUsername());
        }
        if (problema != null) {
            request.setAttribute("error", problema);
            request.setAttribute("usuarioClave", destino);
            mostrar(request, response);
            return;
        }

        dao.cambiarClave(destino.getIdUsuario(), Contrasenas.generarHash(clave), true);
        LOG.info("Contraseña de '" + destino.getUsername() + "' restablecida por "
                + Seguridad.usuarioActual(request).getUsername());
        Mensajes.ok(response, PAGINA, "Contraseña restablecida. La persona deberá cambiarla al iniciar sesión.");
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("listaUsuarios", dao.listarTodos());
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
