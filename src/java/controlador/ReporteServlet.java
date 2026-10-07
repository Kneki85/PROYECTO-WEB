package controlador;

import datos.ReporteDAO;
import modelo.ConteoTipo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Reportes: incidencias por tipo y empleados con más incidencias. */
@WebServlet("/ReporteServlet")
public class ReporteServlet extends HttpServlet {

    private static final int TOP_EMPLEADOS = 10;

    private final ReporteDAO dao = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ConteoTipo> porTipo = dao.incidenciasPorTipo();
        int maximo = porTipo.stream().mapToInt(ConteoTipo::total).max().orElse(0);

        request.setAttribute("porTipo", porTipo);
        request.setAttribute("maximoPorTipo", maximo);
        request.setAttribute("topEmpleados", dao.empleadosConMasIncidencias(TOP_EMPLEADOS));
        request.getRequestDispatcher("/WEB-INF/vistas/reportes.jsp").forward(request, response);
    }
}
