<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String activa = "cuenta";
    String tituloPagina = "Mi cuenta";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
    <section class="tarjeta">
        <div class="titulo-fila"><h2>Mi cuenta</h2></div>

        <p>Usuario: <strong><%= Vista.escapar(usuarioSesion.getUsername()) %></strong>
           · Rol: <strong><%= usuarioSesion.getRol().getEtiqueta() %></strong></p>

        <% if (usuarioSesion.isDebeCambiarClave()) { %>
        <div class="aviso aviso-error" role="alert"><%= Vista.icono("alerta") %>
            <span>Tu contraseña es temporal. Debes cambiarla para poder usar el sistema.</span></div>
        <% } %>

        <h3>Cambiar contraseña</h3>
        <form class="formulario" method="post" action="<%= ctx %>/CuentaServlet" autocomplete="off">
            <%= Seguridad.campoCsrf(request) %>

            <label for="claveActual">Contraseña actual</label>
            <input type="password" id="claveActual" name="claveActual" required maxlength="128" autocomplete="current-password">

            <label for="clave">Contraseña nueva</label>
            <div>
                <input type="password" id="clave" name="clave" required minlength="8" maxlength="128" autocomplete="new-password">
                <span class="ayuda">Mínimo 8 caracteres, con letras y números.</span>
            </div>

            <label for="confirmacion">Repite la nueva</label>
            <input type="password" id="confirmacion" name="confirmacion" required minlength="8" maxlength="128" autocomplete="new-password">

            <div class="formulario-botones">
                <button type="submit" class="btn btn-primario">Cambiar contraseña</button>
            </div>
        </form>
    </section>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
