package controlador;

import datos.DatosException;
import datos.EmpleadoDAO;
import modelo.Empleado;
import seguridad.Seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Empleados. Todos los usuarios pueden ver el listado; crear, editar y eliminar
 * es solo para administradores (y las eliminaciones solo se aceptan por POST).
 */
@WebServlet("/EmpleadoServlet")
public class EmpleadoServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(EmpleadoServlet.class.getName());
    private static final int MAX_CAMPO = 80;
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1970, 1, 1);
    private static final String PAGINA = "EmpleadoServlet";
    private static final String VISTA_LISTA = "/WEB-INF/vistas/empleados/listar.jsp";
    private static final String VISTA_FORM = "/WEB-INF/vistas/empleados/formulario.jsp";

    private final EmpleadoDAO dao = new EmpleadoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "nuevo":
                if (!Seguridad.exigirAdmin(request, response)) return;
                mostrarFormulario(request, response, null);
                break;

            case "editar": {
                if (!Seguridad.exigirAdmin(request, response)) return;
                Integer id = Peticion.aEntero(request.getParameter("id"));
                Empleado empleado = (id != null) ? dao.buscarPorId(id) : null;
                if (empleado == null) {
                    Mensajes.error(response, PAGINA, "El empleado que intenta editar no existe.");
                    return;
                }
                mostrarFormulario(request, response, empleado);
                break;
            }

            default:
                request.setAttribute("listaEmpleados", dao.listarTodos());
                request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Seguridad.exigirAdmin(request, response)) return;
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            eliminar(request, response);
            return;
        }
        if (!"registrar".equals(accion) && !"actualizar".equals(accion)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            return;
        }
        guardar(request, response, "actualizar".equals(accion));
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = Peticion.aEntero(request.getParameter("id"));
        if (id == null) {
            Mensajes.error(response, PAGINA, "Identificador de empleado inválido.");
            return;
        }
        if (dao.eliminar(id)) {
            LOG.info("Empleado " + id + " eliminado por " + Seguridad.usuarioActual(request).getUsername());
            Mensajes.ok(response, PAGINA, "Empleado eliminado correctamente (junto con sus incidencias).");
        } else {
            Mensajes.error(response, PAGINA, "No se pudo eliminar el empleado.");
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, boolean editando)
            throws ServletException, IOException {
        Integer id = null;
        if (editando) {
            id = Peticion.aEntero(request.getParameter("id"));
            if (id == null) {
                Mensajes.error(response, PAGINA, "Identificador de empleado inválido.");
                return;
            }
        }

        String nombre = Peticion.valor(request.getParameter("nombre"));
        String apellido = Peticion.valor(request.getParameter("apellido"));
        String cargo = Peticion.valor(request.getParameter("cargo"));
        String area = Peticion.valor(request.getParameter("area"));
        String fechaTexto = Peticion.valor(request.getParameter("fechaIngreso"));

        // Objeto con lo que escribió el usuario, para no perder sus datos si hay un error
        Empleado empleado = new Empleado(id == null ? 0 : id, nombre, apellido, cargo, area, null);

        LocalDate fecha = fechaTexto.isEmpty() ? LocalDate.now() : Peticion.aFecha(fechaTexto);
        String problema = null;
        if (nombre.isEmpty()) {
            problema = "El nombre es obligatorio.";
        } else if (apellido.isEmpty()) {
            problema = "El apellido es obligatorio.";
        } else if (nombre.length() > MAX_CAMPO || apellido.length() > MAX_CAMPO
                || cargo.length() > MAX_CAMPO || area.length() > MAX_CAMPO) {
            problema = "Ningún campo puede superar los " + MAX_CAMPO + " caracteres.";
        } else if (fecha == null) {
            problema = "La fecha de ingreso no es válida.";
        } else if (fecha.isAfter(LocalDate.now())) {
            problema = "La fecha de ingreso no puede ser futura.";
        } else if (fecha.isBefore(FECHA_MINIMA)) {
            problema = "La fecha de ingreso es demasiado antigua.";
        }

        if (fecha != null) empleado.setFechaIngreso(Date.valueOf(fecha));
        if (problema != null) {
            request.setAttribute("error", problema);
            mostrarFormulario(request, response, empleado);
            return;
        }

        boolean guardado;
        try {
            guardado = editando ? dao.actualizar(empleado) : dao.registrar(empleado);
        } catch (DatosException e) {
            LOG.log(Level.SEVERE, "No se pudo guardar el empleado", e);
            request.setAttribute("error", "No se pudo guardar el empleado. Intenta de nuevo.");
            mostrarFormulario(request, response, empleado);
            return;
        }

        if (guardado) {
            Mensajes.ok(response, PAGINA,
                    editando ? "Empleado actualizado correctamente." : "Empleado registrado correctamente.");
        } else {
            Mensajes.error(response, PAGINA, "No se encontró el empleado que intentabas guardar.");
        }
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, Empleado empleado)
            throws ServletException, IOException {
        request.setAttribute("empleado", empleado);
        request.getRequestDispatcher(VISTA_FORM).forward(request, response);
    }
}
