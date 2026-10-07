package controlador;

import datos.DatosException;
import datos.EmpleadoDAO;
import datos.HistorialDAO;
import datos.IncidenciaDAO;
import datos.TipoIncidenciaDAO;
import modelo.Estados;
import modelo.FiltroIncidencias;
import modelo.Incidencia;
import modelo.Usuario;
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
 * Incidencias: listado con filtros y paginación, formulario de registro/edición e historial.
 * Todos los usuarios pueden consultar; registrar, editar y eliminar es solo para administradores,
 * y las eliminaciones solo se aceptan por POST (un enlace no puede borrar datos).
 */
@WebServlet("/IncidenciaServlet")
public class IncidenciaServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(IncidenciaServlet.class.getName());
    private static final int TAMANO_PAGINA = 10;
    private static final int MAX_DESCRIPCION = 1000;
    private static final LocalDate FECHA_MINIMA = LocalDate.of(2000, 1, 1);
    private static final String PAGINA = "IncidenciaServlet";
    private static final String VISTA_LISTA = "/WEB-INF/vistas/incidencias/listar.jsp";
    private static final String VISTA_FORM = "/WEB-INF/vistas/incidencias/formulario.jsp";
    private static final String VISTA_HISTORIAL = "/WEB-INF/vistas/incidencias/historial.jsp";

    private final IncidenciaDAO dao = new IncidenciaDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final TipoIncidenciaDAO tipoDAO = new TipoIncidenciaDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "nuevo": {
                if (!Seguridad.exigirAdmin(request, response)) return;
                Incidencia nueva = new Incidencia();
                nueva.setEstado(Estados.ABIERTA);
                nueva.setFechaRegistro(Date.valueOf(LocalDate.now()));
                mostrarFormulario(request, response, nueva, false);
                break;
            }

            case "editar": {
                if (!Seguridad.exigirAdmin(request, response)) return;
                Integer id = Peticion.aEntero(request.getParameter("id"));
                Incidencia inc = (id != null) ? dao.buscarPorId(id) : null;
                if (inc == null) {
                    Mensajes.error(response, PAGINA, "La incidencia que intenta editar no existe.");
                    return;
                }
                mostrarFormulario(request, response, inc, true);
                break;
            }

            case "historial": {
                Integer id = Peticion.aEntero(request.getParameter("id"));
                Incidencia inc = (id != null) ? dao.buscarPorId(id) : null;
                if (inc == null) {
                    Mensajes.error(response, PAGINA, "La incidencia solicitada no existe.");
                    return;
                }
                request.setAttribute("incidencia", inc);
                request.setAttribute("listaHistorial", historialDAO.listarPorIncidencia(inc.getIdIncidencia()));
                request.getRequestDispatcher(VISTA_HISTORIAL).forward(request, response);
                break;
            }

            default:
                listar(request, response);
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

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        FiltroIncidencias filtro = Peticion.filtroIncidencias(request);
        int pagina = Peticion.entero(request.getParameter("pagina"), 1);

        request.setAttribute("filtro", filtro);
        request.setAttribute("pagina", dao.buscar(filtro, pagina, TAMANO_PAGINA));
        request.setAttribute("mapaTipos", tipoDAO.listarTodos());
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = Peticion.aEntero(request.getParameter("id"));
        if (id == null) {
            Mensajes.error(response, PAGINA, "Identificador de incidencia inválido.");
            return;
        }
        if (dao.eliminar(id)) {
            LOG.info("Incidencia " + id + " eliminada por " + Seguridad.usuarioActual(request).getUsername());
            Mensajes.ok(response, PAGINA, "Incidencia eliminada correctamente.");
        } else {
            Mensajes.error(response, PAGINA, "No se pudo eliminar la incidencia.");
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, boolean editando)
            throws ServletException, IOException {
        Integer idIncidencia = null;
        if (editando) {
            idIncidencia = Peticion.aEntero(request.getParameter("id"));
            if (idIncidencia == null) {
                Mensajes.error(response, PAGINA, "Identificador de incidencia inválido.");
                return;
            }
        }

        Integer idEmpleado = Peticion.aEntero(request.getParameter("idEmpleado"));
        Integer idTipo = Peticion.aEntero(request.getParameter("idTipo"));
        String descripcion = Peticion.valor(request.getParameter("descripcion"));
        String fechaTexto = Peticion.valor(request.getParameter("fechaRegistro"));
        String estado = request.getParameter("estado");

        LocalDate fecha = fechaTexto.isEmpty() ? LocalDate.now() : Peticion.aFecha(fechaTexto);

        // Objeto con lo que escribió el usuario, para no perder sus datos si hay un error
        Incidencia inc = new Incidencia();
        inc.setIdIncidencia(idIncidencia == null ? 0 : idIncidencia);
        inc.setIdEmpleado(idEmpleado == null ? 0 : idEmpleado);
        inc.setIdTipo(idTipo == null ? 0 : idTipo);
        inc.setDescripcion(descripcion);
        inc.setEstado(estado);
        if (fecha != null) inc.setFechaRegistro(Date.valueOf(fecha));

        String problema = validar(inc, fecha);
        if (problema != null) {
            request.setAttribute("error", problema);
            mostrarFormulario(request, response, inc, editando);
            return;
        }

        Usuario usuario = Seguridad.usuarioActual(request);
        boolean guardado;
        try {
            guardado = editando ? dao.actualizar(inc, usuario.getIdUsuario())
                                : dao.registrar(inc, usuario.getIdUsuario());
        } catch (DatosException e) {
            LOG.log(Level.SEVERE, "No se pudo guardar la incidencia", e);
            request.setAttribute("error", "No se pudo guardar la incidencia. Intenta de nuevo.");
            mostrarFormulario(request, response, inc, editando);
            return;
        }

        if (guardado) {
            Mensajes.ok(response, PAGINA,
                    editando ? "Incidencia actualizada correctamente." : "Incidencia registrada correctamente.");
        } else {
            request.setAttribute("error", "No se pudo guardar. Verifica que el empleado y el tipo existan.");
            mostrarFormulario(request, response, inc, editando);
        }
    }

    /** Devuelve el mensaje del primer problema encontrado, o null si todo está bien. */
    private String validar(Incidencia inc, LocalDate fecha) {
        if (inc.getIdEmpleado() <= 0) return "Seleccione un empleado.";
        if (inc.getIdTipo() <= 0) return "Seleccione un tipo de incidencia.";
        if (inc.getDescripcion().isEmpty()) return "La descripción no puede estar vacía.";
        if (inc.getDescripcion().length() > MAX_DESCRIPCION) {
            return "La descripción no puede superar los " + MAX_DESCRIPCION + " caracteres.";
        }
        if (!Estados.esValido(inc.getEstado())) return "El estado seleccionado no es válido.";
        if (fecha == null) return "La fecha no es válida.";
        if (fecha.isAfter(LocalDate.now())) return "La fecha no puede ser futura.";
        if (fecha.isBefore(FECHA_MINIMA)) return "La fecha es demasiado antigua (mínimo 01/01/2000).";
        return null;
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Incidencia inc, boolean editando) throws ServletException, IOException {
        request.setAttribute("incidencia", inc);
        request.setAttribute("editando", editando);
        request.setAttribute("listaEmpleados", empleadoDAO.listarTodos());
        request.setAttribute("mapaTipos", tipoDAO.listarTodos());
        request.getRequestDispatcher(VISTA_FORM).forward(request, response);
    }
}
