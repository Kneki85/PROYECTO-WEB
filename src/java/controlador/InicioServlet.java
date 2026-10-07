package controlador;

import datos.IncidenciaDAO;
import datos.ReporteDAO;
import modelo.FiltroIncidencias;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Pantalla de inicio: resumen general y últimas incidencias. */
@WebServlet("/InicioServlet")
public class InicioServlet extends HttpServlet {

    private static final int ULTIMAS = 5;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final IncidenciaDAO incidenciaDAO = new IncidenciaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("resumen", reporteDAO.resumen());
        request.setAttribute("ultimas", incidenciaDAO.buscar(new FiltroIncidencias(), 1, ULTIMAS));
        request.getRequestDispatcher("/WEB-INF/vistas/inicio.jsp").forward(request, response);
    }
}
