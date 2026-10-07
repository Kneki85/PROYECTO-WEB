<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, modelo.Empleado" %>
<%
    String activa = "empleados";
    String tituloPagina = "Empleados";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Empleado> lista = (List<Empleado>) request.getAttribute("listaEmpleados");
%>
    <section class="tarjeta">
        <div class="titulo-fila">
            <h2>Empleados registrados</h2>
            <div class="acciones-grupo">
                <a class="btn btn-secundario" href="<%= ctx %>/ExportarServlet?tipo=empleados"><%= Vista.icono("descargar") %>Exportar CSV</a>
                <% if (esAdmin) { %>
                <a class="btn btn-primario" href="<%= ctx %>/EmpleadoServlet?accion=nuevo"><%= Vista.icono("mas") %>Nuevo empleado</a>
                <% } %>
            </div>
        </div>

        <% if (lista.isEmpty()) { %>
        <p class="vacio">No hay empleados registrados todavía.</p>
        <% } else { %>
        <div class="tabla-scroll">
        <table class="tabla">
            <thead>
                <tr><th>ID</th><th>Nombre</th><th>Cargo</th><th>Área</th><th>Ingreso</th><% if (esAdmin) { %><th>Acciones</th><% } %></tr>
            </thead>
            <tbody>
            <% for (Empleado e : lista) { %>
                <tr>
                    <td><%= e.getIdEmpleado() %></td>
                    <td><%= Vista.escapar(e.getNombreCompleto()) %></td>
                    <td><%= Vista.escapar(e.getCargo()) %></td>
                    <td><%= Vista.escapar(e.getArea()) %></td>
                    <td><%= Vista.fecha(e.getFechaIngreso()) %></td>
                    <% if (esAdmin) { %>
                    <td>
                        <div class="acciones">
                            <a class="icono-btn" title="Editar" aria-label="Editar a <%= Vista.escapar(e.getNombreCompleto()) %>"
                               href="<%= ctx %>/EmpleadoServlet?accion=editar&amp;id=<%= e.getIdEmpleado() %>"><%= Vista.icono("editar") %></a>
                            <form class="form-inline" method="post" action="<%= ctx %>/EmpleadoServlet"
                                  onsubmit="return confirm('¿Eliminar a este empleado? También se eliminarán todas sus incidencias.');">
                                <%= Seguridad.campoCsrf(request) %>
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="<%= e.getIdEmpleado() %>">
                                <button type="submit" class="icono-btn icono-eliminar" title="Eliminar"
                                        aria-label="Eliminar a <%= Vista.escapar(e.getNombreCompleto()) %>"><%= Vista.icono("eliminar") %></button>
                            </form>
                        </div>
                    </td>
                    <% } %>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>
        <p class="texto-suave" style="margin-top:12px">Total: <%= lista.size() %> empleado(s)</p>
        <% } %>
    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
