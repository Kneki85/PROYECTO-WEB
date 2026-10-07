<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, modelo.Historial, modelo.Incidencia" %>
<%
    String activa = "incidencias";
    String tituloPagina = "Historial de incidencia";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    Incidencia inc = (Incidencia) request.getAttribute("incidencia");
    @SuppressWarnings("unchecked")
    List<Historial> historial = (List<Historial>) request.getAttribute("listaHistorial");
%>
    <section class="tarjeta">
        <div class="titulo-fila">
            <h2>Historial de la incidencia #<%= inc.getIdIncidencia() %></h2>
            <a class="btn btn-secundario" href="<%= ctx %>/IncidenciaServlet">Volver al listado</a>
        </div>

        <p>
            <strong><%= Vista.escapar(inc.getEmpleadoNombre()) %></strong> ·
            <span class="chip chip-tipo"><%= Vista.escapar(inc.getTipoNombre()) %></span> ·
            <%= Vista.fecha(inc.getFechaRegistro()) %> ·
            <span class="<%= Vista.claseEstado(inc.getEstado()) %>"><%= Vista.escapar(inc.getEstado()) %></span>
        </p>
        <p class="texto-suave"><%= Vista.escapar(inc.getDescripcion()) %></p>

        <h3>Cambios de estado</h3>
        <% if (historial.isEmpty()) { %>
        <p class="vacio">Esta incidencia no tiene cambios registrados.</p>
        <% } else { %>
        <div class="tabla-scroll">
        <table class="tabla">
            <thead>
                <tr><th>Fecha y hora</th><th>Cambio</th><th>Realizado por</th></tr>
            </thead>
            <tbody>
            <% for (Historial h : historial) { %>
                <tr>
                    <td><%= Vista.fechaHora(h.getFechaCambio()) %></td>
                    <td>
                        <% if (h.getEstadoAnterior() == null) { %>
                        Registro inicial &rarr; <span class="<%= Vista.claseEstado(h.getEstadoNuevo()) %>"><%= Vista.escapar(h.getEstadoNuevo()) %></span>
                        <% } else { %>
                        <span class="<%= Vista.claseEstado(h.getEstadoAnterior()) %>"><%= Vista.escapar(h.getEstadoAnterior()) %></span>
                        &rarr; <span class="<%= Vista.claseEstado(h.getEstadoNuevo()) %>"><%= Vista.escapar(h.getEstadoNuevo()) %></span>
                        <% } %>
                    </td>
                    <td><%= h.getUsuario() == null ? "—" : Vista.escapar(h.getUsuario()) %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>
        <% } %>
    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
