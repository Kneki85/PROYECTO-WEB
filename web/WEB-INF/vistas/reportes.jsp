<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, modelo.ConteoTipo, modelo.EmpleadoIncidencias" %>
<%
    String activa = "reportes";
    String tituloPagina = "Reportes";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<ConteoTipo> porTipo = (List<ConteoTipo>) request.getAttribute("porTipo");
    int maximo = (Integer) request.getAttribute("maximoPorTipo");
    @SuppressWarnings("unchecked")
    List<EmpleadoIncidencias> top = (List<EmpleadoIncidencias>) request.getAttribute("topEmpleados");
%>
    <section class="tarjeta">
        <div class="titulo-fila">
            <h2>Incidencias por tipo</h2>
            <div class="acciones-grupo no-imprimir">
                <a class="btn btn-secundario" href="<%= ctx %>/ExportarServlet?tipo=incidencias"><%= Vista.icono("descargar") %>Exportar CSV</a>
                <button type="button" class="btn btn-secundario" onclick="window.print()"><%= Vista.icono("imprimir") %>Imprimir / guardar PDF</button>
            </div>
        </div>

        <% if (porTipo.isEmpty()) { %>
        <p class="vacio">No hay tipos de incidencia registrados.</p>
        <% } %>
        <% for (ConteoTipo c : porTipo) {
               int porcentaje = maximo == 0 ? 0 : (int) Math.round(c.total() * 100.0 / maximo); %>
        <div class="barra-fila">
            <span><%= Vista.escapar(c.tipo()) %></span>
            <div class="barra-pista" role="img" aria-label="<%= Vista.escapar(c.tipo()) %>: <%= c.total() %>">
                <div class="barra" style="width:<%= porcentaje %>%"></div>
            </div>
            <span class="barra-valor"><%= c.total() %></span>
        </div>
        <% } %>

        <h3>Empleados con más incidencias</h3>
        <% if (top.isEmpty()) { %>
        <p class="vacio">Aún no hay incidencias registradas.</p>
        <% } else { %>
        <div class="tabla-scroll">
        <table class="tabla">
            <thead>
                <tr><th>Empleado</th><th class="num">Total</th><th class="num">Abiertas</th></tr>
            </thead>
            <tbody>
            <% for (EmpleadoIncidencias e : top) { %>
                <tr>
                    <td><%= Vista.escapar(e.nombre()) %></td>
                    <td class="num"><%= e.total() %></td>
                    <td class="num"><%= e.abiertas() %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>
        <% } %>
    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
