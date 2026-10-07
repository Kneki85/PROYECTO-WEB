package controlador;

import datos.EmpleadoDAO;
import datos.IncidenciaDAO;
import modelo.Empleado;
import modelo.FiltroIncidencias;
import modelo.Incidencia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

/**
 * Exporta datos a CSV (se abre directo en Excel).
 *   /ExportarServlet?tipo=empleados
 *   /ExportarServlet?tipo=incidencias[&texto=..&estado=..&idTipo=..&desde=..&hasta=..]
 * En incidencias se respetan los mismos filtros del buscador. Disponible para todos los usuarios.
 */
@WebServlet("/ExportarServlet")
public class ExportarServlet extends HttpServlet {

    // Separador ";" porque Excel en español lo usa por defecto
    private static final String SEP = ";";

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final IncidenciaDAO incidenciaDAO = new IncidenciaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tipo = request.getParameter("tipo");
        if (tipo == null) tipo = "";

        switch (tipo) {
            case "empleados":
                exportarEmpleados(response);
                break;
            case "incidencias":
                exportarIncidencias(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Parámetro 'tipo' inválido. Use empleados o incidencias.");
        }
    }

    private void exportarEmpleados(HttpServletResponse response) throws IOException {
        // Se leen los datos ANTES de abrir la respuesta: si la BD falla, se muestra la página de error
        java.util.List<Empleado> lista = empleadoDAO.listarTodos();

        PrintWriter out = abrirCsv(response, "empleados");
        out.print("ID" + SEP + "Nombre" + SEP + "Apellido" + SEP + "Cargo" + SEP + "Area" + SEP + "Fecha ingreso\r\n");
        for (Empleado e : lista) {
            out.print(e.getIdEmpleado() + SEP
                    + csv(e.getNombre()) + SEP
                    + csv(e.getApellido()) + SEP
                    + csv(e.getCargo()) + SEP
                    + csv(e.getArea()) + SEP
                    + (e.getFechaIngreso() == null ? "" : e.getFechaIngreso()) + "\r\n");
        }
        out.flush();
    }

    private void exportarIncidencias(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        FiltroIncidencias filtro = Peticion.filtroIncidencias(request);
        java.util.List<Incidencia> lista = incidenciaDAO.buscarTodas(filtro);

        PrintWriter out = abrirCsv(response, "incidencias");
        out.print("ID" + SEP + "Empleado" + SEP + "Tipo" + SEP + "Descripcion"
                + SEP + "Fecha" + SEP + "Estado\r\n");
        for (Incidencia inc : lista) {
            out.print(inc.getIdIncidencia() + SEP
                    + csv(inc.getEmpleadoNombre()) + SEP
                    + csv(inc.getTipoNombre()) + SEP
                    + csv(inc.getDescripcion()) + SEP
                    + inc.getFechaRegistro() + SEP
                    + csv(inc.getEstado()) + "\r\n");
        }
        out.flush();
    }

    /** Prepara la respuesta como descarga CSV en UTF-8 (con BOM para que Excel respete tildes y ñ). */
    private PrintWriter abrirCsv(HttpServletResponse response, String nombreBase) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + nombreBase + "_" + LocalDate.now() + ".csv\"");
        PrintWriter out = response.getWriter();
        out.print('\uFEFF'); // BOM UTF-8
        return out;
    }

    /**
     * Escapa un texto para CSV: encierra en comillas y duplica las comillas internas.
     * Además neutraliza la "inyección de fórmulas": si el texto empieza con = + - @ Excel lo
     * ejecutaría como fórmula, así que se le antepone un apóstrofo para que se lea como texto.
     */
    private String csv(String valor) {
        if (valor == null) return "";
        String limpio = valor.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"");
        if (!limpio.isEmpty() && "=+-@\t".indexOf(limpio.charAt(0)) >= 0) {
            limpio = "'" + limpio;
        }
        return "\"" + limpio + "\"";
    }
}
