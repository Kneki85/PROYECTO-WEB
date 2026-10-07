<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map, modelo.Empleado, modelo.Estados, modelo.Incidencia" %>
<%
    String activa = "incidencias";
    boolean editando = Boolean.TRUE.equals(request.getAttribute("editando"));
    String tituloPagina = editando ? "Editar incidencia" : "Nueva incidencia";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    Incidencia inc = (Incidencia) request.getAttribute("incidencia");
    @SuppressWarnings("unchecked")
    List<Empleado> empleados = (List<Empleado>) request.getAttribute("listaEmpleados");
    @SuppressWarnings("unchecked")
    Map<Integer, String> tipos = (Map<Integer, String>) request.getAttribute("mapaTipos");

    String fecha = inc.getFechaRegistro() == null ? java.time.LocalDate.now().toString() : inc.getFechaRegistro().toString();
    String descripcion = inc.getDescripcion() == null ? "" : inc.getDescripcion();
    String estadoActual = inc.getEstado() == null ? Estados.ABIERTA : inc.getEstado();
%>
    <section class="tarjeta">
        <div class="titulo-fila"><h2><%= tituloPagina %><%= editando ? " #" + inc.getIdIncidencia() : "" %></h2></div>

        <% if (empleados.isEmpty()) { %>
        <p class="vacio">Primero debes <a href="<%= ctx %>/EmpleadoServlet?accion=nuevo">registrar un empleado</a>.</p>
        <% } else { %>
        <form class="formulario" method="post" action="<%= ctx %>/IncidenciaServlet">
            <%= Seguridad.campoCsrf(request) %>
            <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "registrar" %>">
            <% if (editando) { %><input type="hidden" name="id" value="<%= inc.getIdIncidencia() %>"><% } %>

            <label for="idEmpleado">Empleado</label>
            <select id="idEmpleado" name="idEmpleado" required>
                <option value="">-- Selecciona --</option>
                <% for (Empleado e : empleados) { %>
                <option value="<%= e.getIdEmpleado() %>"<%= e.getIdEmpleado() == inc.getIdEmpleado() ? " selected" : "" %>><%= Vista.escapar(e.getNombreCompleto()) %></option>
                <% } %>
            </select>

            <label for="idTipo">Tipo</label>
            <select id="idTipo" name="idTipo" required>
                <option value="">-- Selecciona --</option>
                <% for (Map.Entry<Integer, String> t : tipos.entrySet()) { %>
                <option value="<%= t.getKey() %>"<%= t.getKey() == inc.getIdTipo() ? " selected" : "" %>><%= Vista.escapar(t.getValue()) %></option>
                <% } %>
            </select>

            <label for="fechaRegistro">Fecha</label>
            <input type="date" id="fechaRegistro" name="fechaRegistro" required min="2000-01-01"
                   max="<%= java.time.LocalDate.now() %>" value="<%= fecha %>">

            <label for="estado">Estado</label>
            <div>
                <select id="estado" name="estado" required>
                    <% for (String e : Estados.TODOS) { %>
                    <option value="<%= Vista.escapar(e) %>"<%= e.equals(estadoActual) ? " selected" : "" %>><%= Vista.escapar(e) %></option>
                    <% } %>
                </select>
                <% if (editando) { %><span class="ayuda">Si cambias el estado, queda registrado en el historial.</span><% } %>
            </div>

            <label for="descripcion">Descripción</label>
            <textarea id="descripcion" name="descripcion" required maxlength="1000"><%= Vista.escapar(descripcion) %></textarea>

            <div class="formulario-botones">
                <button type="submit" class="btn btn-primario"><%= editando ? "Guardar cambios" : "Registrar incidencia" %></button>
                <a class="btn btn-secundario" href="<%= ctx %>/IncidenciaServlet">Cancelar</a>
            </div>
        </form>
        <% } %>
    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
