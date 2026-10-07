<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="modelo.Empleado" %>
<%
    String activa = "empleados";
    Empleado emp = (Empleado) request.getAttribute("empleado");
    boolean editando = emp != null && emp.getIdEmpleado() > 0;
    String tituloPagina = editando ? "Editar empleado" : "Nuevo empleado";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    String nombre = emp == null ? "" : emp.getNombre();
    String apellido = emp == null ? "" : emp.getApellido();
    String cargo = emp == null ? "" : emp.getCargo();
    String area = emp == null ? "" : emp.getArea();
    String fecha = (emp == null || emp.getFechaIngreso() == null)
            ? java.time.LocalDate.now().toString() : emp.getFechaIngreso().toString();
%>
    <section class="tarjeta">
        <div class="titulo-fila"><h2><%= tituloPagina %></h2></div>

        <form class="formulario" method="post" action="<%= ctx %>/EmpleadoServlet">
            <%= Seguridad.campoCsrf(request) %>
            <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "registrar" %>">
            <% if (editando) { %><input type="hidden" name="id" value="<%= emp.getIdEmpleado() %>"><% } %>

            <label for="nombre">Nombre</label>
            <input type="text" id="nombre" name="nombre" required maxlength="80" value="<%= Vista.escapar(nombre) %>">

            <label for="apellido">Apellido</label>
            <input type="text" id="apellido" name="apellido" required maxlength="80" value="<%= Vista.escapar(apellido) %>">

            <label for="cargo">Cargo</label>
            <input type="text" id="cargo" name="cargo" maxlength="80" value="<%= Vista.escapar(cargo) %>">

            <label for="area">Área</label>
            <input type="text" id="area" name="area" maxlength="80" value="<%= Vista.escapar(area) %>">

            <label for="fechaIngreso">Fecha de ingreso</label>
            <input type="date" id="fechaIngreso" name="fechaIngreso" min="1970-01-01"
                   max="<%= java.time.LocalDate.now() %>" value="<%= fecha %>">

            <div class="formulario-botones">
                <button type="submit" class="btn btn-primario"><%= editando ? "Guardar cambios" : "Registrar empleado" %></button>
                <a class="btn btn-secundario" href="<%= ctx %>/EmpleadoServlet">Cancelar</a>
            </div>
        </form>
    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
