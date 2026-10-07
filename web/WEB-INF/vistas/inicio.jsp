<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="modelo.Incidencia, modelo.Pagina, modelo.Resumen" %>
<%
    String activa = "inicio";
    String tituloPagina = "Inicio";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    Resumen resumen = (Resumen) request.getAttribute("resumen");
    @SuppressWarnings("unchecked")
    Pagina<Incidencia> ultimas = (Pagina<Incidencia>) request.getAttribute("ultimas");
%>
    <section class="tarjeta bienvenida">
        <% if (esAdmin) { %>
        <div>
            <h2>Panel del administrador de personal</h2>
            <p>Hola, <%= Vista.escapar(usuarioSesion.getNombreCompleto()) %>. Desde aquí registras empleados e incidencias, cambias su estado, revisas el historial y administras los usuarios del sistema.</p>
        </div>
        <div class="acciones-grupo">
            <a class="btn btn-primario" href="<%= ctx %>/IncidenciaServlet?accion=nuevo"><%= Vista.icono("mas") %>Nueva incidencia</a>
            <a class="btn btn-secundario" href="<%= ctx %>/EmpleadoServlet?accion=nuevo"><%= Vista.icono("usuario-mas") %>Nuevo empleado</a>
        </div>
        <% } else { %>
        <div>
            <h2>Panel de personal de consulta</h2>
            <p>Hola, <%= Vista.escapar(usuarioSesion.getNombreCompleto()) %>. Desde aquí consultas empleados, incidencias, historial y reportes. Este perfil es de solo lectura: puedes buscar y descargar la información, pero no modificarla.</p>
        </div>
        <div class="acciones-grupo">
            <a class="btn btn-primario" href="<%= ctx %>/IncidenciaServlet"><%= Vista.icono("buscar") %>Consultar incidencias</a>
            <a class="btn btn-secundario" href="<%= ctx %>/ExportarServlet?tipo=incidencias"><%= Vista.icono("descargar") %>Descargar CSV</a>
        </div>
        <% } %>
    </section>

    <section class="tarjeta">
        <h2>Resumen general</h2>
        <div class="resumen-grid">
            <div class="resumen-item"><div class="etiqueta">Empleados registrados</div><div class="valor"><%= resumen.empleados() %></div></div>
            <div class="resumen-item"><div class="etiqueta">Incidencias abiertas</div><div class="valor"><%= resumen.abiertas() %></div></div>
            <div class="resumen-item"><div class="etiqueta">En proceso</div><div class="valor"><%= resumen.enProceso() %></div></div>
            <div class="resumen-item"><div class="etiqueta">Cerradas</div><div class="valor"><%= resumen.cerradas() %></div></div>
        </div>

        <div class="titulo-fila">
            <h2>Últimas incidencias</h2>
            <a href="<%= ctx %>/IncidenciaServlet">Ver todas</a>
        </div>

        <% if (ultimas.items().isEmpty()) { %>
        <p class="vacio">Aún no hay incidencias registradas.</p>
        <% } else { %>
        <div class="tabla-scroll">
        <table class="tabla">
            <thead>
                <tr><th>Empleado</th><th>Tipo</th><th>Fecha</th><th>Estado</th></tr>
            </thead>
            <tbody>
            <% for (Incidencia inc : ultimas.items()) { %>
                <tr>
                    <td><%= Vista.escapar(inc.getEmpleadoNombre()) %></td>
                    <td><%= Vista.escapar(inc.getTipoNombre()) %></td>
                    <td><%= Vista.fecha(inc.getFechaRegistro()) %></td>
                    <td><span class="<%= Vista.claseEstado(inc.getEstado()) %>"><%= Vista.escapar(inc.getEstado()) %></span></td>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>
        <% } %>

    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
