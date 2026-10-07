<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map, modelo.Estados, modelo.FiltroIncidencias, modelo.Incidencia, modelo.Pagina" %>
<%
    String activa = "incidencias";
    String tituloPagina = "Incidencias";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    FiltroIncidencias filtro = (FiltroIncidencias) request.getAttribute("filtro");
    @SuppressWarnings("unchecked")
    Pagina<Incidencia> pag = (Pagina<Incidencia>) request.getAttribute("pagina");
    @SuppressWarnings("unchecked")
    Map<Integer, String> tipos = (Map<Integer, String>) request.getAttribute("mapaTipos");

    // Los enlaces de paginación y de exportación conservan los filtros activos
    String qs = filtro.aQueryString();
    String base = ctx + "/IncidenciaServlet?" + (qs.isEmpty() ? "" : qs + "&");
    String urlExportar = ctx + "/ExportarServlet?tipo=incidencias" + (qs.isEmpty() ? "" : "&" + qs);
%>
    <section class="tarjeta">
        <div class="titulo-fila">
            <h2>Incidencias registradas</h2>
            <div class="acciones-grupo">
                <a class="btn btn-secundario" href="<%= Vista.escapar(urlExportar) %>"><%= Vista.icono("descargar") %>Exportar CSV</a>
                <% if (esAdmin) { %>
                <a class="btn btn-primario" href="<%= ctx %>/IncidenciaServlet?accion=nuevo"><%= Vista.icono("mas") %>Nueva incidencia</a>
                <% } %>
            </div>
        </div>

        <form class="filtros" method="get" action="<%= ctx %>/IncidenciaServlet">
            <div class="campo campo-busqueda">
                <%= Vista.icono("buscar") %>
                <input type="search" name="texto" maxlength="100" placeholder="Buscar por empleado o descripción"
                       aria-label="Buscar por empleado o descripción" value="<%= Vista.escapar(filtro.getTexto()) %>">
            </div>
            <select name="estado" aria-label="Estado">
                <option value="">Estado: todos</option>
                <% for (String e : Estados.TODOS) { %>
                <option value="<%= Vista.escapar(e) %>"<%= e.equals(filtro.getEstado()) ? " selected" : "" %>><%= Vista.escapar(e) %></option>
                <% } %>
            </select>
            <select name="idTipo" aria-label="Tipo">
                <option value="">Tipo: todos</option>
                <% for (Map.Entry<Integer, String> t : tipos.entrySet()) { %>
                <option value="<%= t.getKey() %>"<%= t.getKey().equals(filtro.getIdTipo()) ? " selected" : "" %>><%= Vista.escapar(t.getValue()) %></option>
                <% } %>
            </select>
            <label>Desde <input type="date" name="desde" value="<%= filtro.getDesde() == null ? "" : filtro.getDesde().toString() %>"></label>
            <label>Hasta <input type="date" name="hasta" value="<%= filtro.getHasta() == null ? "" : filtro.getHasta().toString() %>"></label>
            <button type="submit" class="btn btn-primario">Buscar</button>
            <% if (filtro.hayFiltro()) { %>
            <a class="btn btn-secundario" href="<%= ctx %>/IncidenciaServlet">Limpiar</a>
            <% } %>
        </form>

        <% if (pag.items().isEmpty()) { %>
        <p class="vacio"><%= filtro.hayFiltro() ? "Ninguna incidencia coincide con los filtros." : "Aún no hay incidencias registradas." %></p>
        <% } else { %>
        <div class="tabla-scroll">
        <table class="tabla">
            <thead>
                <tr><th>ID</th><th>Empleado</th><th>Tipo</th><th>Fecha</th><th>Estado</th><th>Acciones</th></tr>
            </thead>
            <tbody>
            <% for (Incidencia inc : pag.items()) { %>
                <tr>
                    <td><%= inc.getIdIncidencia() %></td>
                    <td><%= Vista.escapar(inc.getEmpleadoNombre()) %></td>
                    <td><span class="chip chip-tipo" title="<%= Vista.escapar(inc.getDescripcion()) %>"><%= Vista.escapar(inc.getTipoNombre()) %></span></td>
                    <td><%= Vista.fecha(inc.getFechaRegistro()) %></td>
                    <td><span class="<%= Vista.claseEstado(inc.getEstado()) %>"><%= Vista.escapar(inc.getEstado()) %></span></td>
                    <td>
                        <div class="acciones">
                            <% if (esAdmin) { %>
                            <a class="icono-btn" title="Editar" aria-label="Editar la incidencia <%= inc.getIdIncidencia() %>"
                               href="<%= ctx %>/IncidenciaServlet?accion=editar&amp;id=<%= inc.getIdIncidencia() %>"><%= Vista.icono("editar") %></a>
                            <% } %>
                            <a class="icono-btn" title="Ver historial" aria-label="Ver el historial de la incidencia <%= inc.getIdIncidencia() %>"
                               href="<%= ctx %>/IncidenciaServlet?accion=historial&amp;id=<%= inc.getIdIncidencia() %>"><%= Vista.icono("historial") %></a>
                            <% if (esAdmin) { %>
                            <form class="form-inline" method="post" action="<%= ctx %>/IncidenciaServlet"
                                  onsubmit="return confirm('¿Eliminar la incidencia <%= inc.getIdIncidencia() %>? Esta acción no se puede deshacer.');">
                                <%= Seguridad.campoCsrf(request) %>
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="<%= inc.getIdIncidencia() %>">
                                <button type="submit" class="icono-btn icono-eliminar" title="Eliminar"
                                        aria-label="Eliminar la incidencia <%= inc.getIdIncidencia() %>"><%= Vista.icono("eliminar") %></button>
                            </form>
                            <% } %>
                        </div>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>

        <nav class="paginacion" aria-label="Paginación">
            <span>Mostrando <%= pag.items().size() %> de <%= pag.total() %> · Página <%= pag.pagina() %> de <%= pag.totalPaginas() %></span>
            <span class="acciones-grupo">
                <a class="btn btn-secundario<%= pag.hayAnterior() ? "" : " deshabilitado" %>"
                   href="<%= Vista.escapar(base + "pagina=" + (pag.pagina() - 1)) %>">Anterior</a>
                <a class="btn btn-secundario<%= pag.haySiguiente() ? "" : " deshabilitado" %>"
                   href="<%= Vista.escapar(base + "pagina=" + (pag.pagina() + 1)) %>">Siguiente</a>
            </span>
        </nav>
        <% } %>
    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
