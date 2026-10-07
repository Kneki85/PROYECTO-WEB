<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%
    String activa = "usuarios";
    String tituloPagina = "Usuarios";
%>
<%@ include file="/WEB-INF/jspf/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Usuario> lista = (List<Usuario>) request.getAttribute("listaUsuarios");
    Usuario usuarioClave = (Usuario) request.getAttribute("usuarioClave");

    String formUsername = (String) request.getAttribute("formUsername");
    String formNombre = (String) request.getAttribute("formNombre");
    String formRol = (String) request.getAttribute("formRol");
    if (formUsername == null) formUsername = "";
    if (formNombre == null) formNombre = "";
    if (formRol == null) formRol = Rol.CONSULTA.name();
%>
    <section class="tarjeta">
        <div class="titulo-fila"><h2>Usuarios del sistema</h2></div>
        <div class="tabla-scroll">
        <table class="tabla">
            <thead>
                <tr><th>Usuario</th><th>Nombre</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr>
            </thead>
            <tbody>
            <% for (Usuario u : lista) {
                   boolean esYo = u.getIdUsuario() == usuarioSesion.getIdUsuario(); %>
                <tr>
                    <td><%= Vista.escapar(u.getUsername()) %><%= esYo ? " (tú)" : "" %></td>
                    <td><%= Vista.escapar(u.getNombreCompleto()) %></td>
                    <td><span class="chip chip-tipo"><%= u.getRol().getEtiqueta() %></span></td>
                    <td>
                        <span class="chip <%= u.isActivo() ? "chip-cerrada" : "chip-inactivo" %>"><%= u.isActivo() ? "Activo" : "Inactivo" %></span>
                        <% if (u.isDebeCambiarClave()) { %><span class="chip chip-abierta">Clave temporal</span><% } %>
                    </td>
                    <td>
                        <div class="acciones">
                            <a class="icono-btn" title="Restablecer contraseña"
                               aria-label="Restablecer la contraseña de <%= Vista.escapar(u.getUsername()) %>"
                               href="<%= ctx %>/UsuarioServlet?accion=clave&amp;id=<%= u.getIdUsuario() %>"><%= Vista.icono("clave") %></a>
                            <% if (!esYo) { %>
                            <form class="form-inline" method="post" action="<%= ctx %>/UsuarioServlet">
                                <%= Seguridad.campoCsrf(request) %>
                                <input type="hidden" name="accion" value="estado">
                                <input type="hidden" name="id" value="<%= u.getIdUsuario() %>">
                                <input type="hidden" name="activo" value="<%= !u.isActivo() %>">
                                <button type="submit" class="btn btn-secundario" style="padding:4px 10px"><%= u.isActivo() ? "Desactivar" : "Activar" %></button>
                            </form>
                            <% } %>
                        </div>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>
    </section>

    <% if (usuarioClave != null) { %>
    <section class="tarjeta">
        <div class="titulo-fila"><h2>Restablecer contraseña de <%= Vista.escapar(usuarioClave.getUsername()) %></h2></div>
        <p class="texto-suave">La persona deberá cambiar esta contraseña temporal la próxima vez que inicie sesión.</p>
        <form class="formulario" method="post" action="<%= ctx %>/UsuarioServlet" autocomplete="off">
            <%= Seguridad.campoCsrf(request) %>
            <input type="hidden" name="accion" value="clave">
            <input type="hidden" name="id" value="<%= usuarioClave.getIdUsuario() %>">

            <label for="rClave">Contraseña temporal</label>
            <div>
                <input type="password" id="rClave" name="clave" required minlength="8" maxlength="128" autocomplete="new-password">
                <span class="ayuda">Mínimo 8 caracteres, con letras y números.</span>
            </div>

            <label for="rConf">Repetir contraseña</label>
            <input type="password" id="rConf" name="confirmacion" required minlength="8" maxlength="128" autocomplete="new-password">

            <div class="formulario-botones">
                <button type="submit" class="btn btn-primario">Restablecer</button>
                <a class="btn btn-secundario" href="<%= ctx %>/UsuarioServlet">Cancelar</a>
            </div>
        </form>
    </section>
    <% } else { %>
    <section class="tarjeta">
        <div class="titulo-fila"><h2>Nuevo usuario</h2></div>
        <form class="formulario" method="post" action="<%= ctx %>/UsuarioServlet" autocomplete="off">
            <%= Seguridad.campoCsrf(request) %>
            <input type="hidden" name="accion" value="crear">

            <label for="username">Usuario</label>
            <div>
                <input type="text" id="username" name="username" required minlength="3" maxlength="50"
                       pattern="[A-Za-z0-9._\-]{3,50}" value="<%= Vista.escapar(formUsername) %>">
                <span class="ayuda">Letras, números, punto, guion o guion bajo.</span>
            </div>

            <label for="nombreCompleto">Nombre completo</label>
            <input type="text" id="nombreCompleto" name="nombreCompleto" required maxlength="120" value="<%= Vista.escapar(formNombre) %>">

            <label for="rol">Rol</label>
            <div>
                <select id="rol" name="rol">
                    <% for (Rol r : Rol.values()) { %>
                    <option value="<%= r.name() %>"<%= r.name().equals(formRol) ? " selected" : "" %>><%= r.getEtiqueta() %></option>
                    <% } %>
                </select>
                <span class="ayuda">Administrador: puede todo. Consulta: solo ve y descarga.</span>
            </div>

            <label for="nClave">Contraseña temporal</label>
            <div>
                <input type="password" id="nClave" name="clave" required minlength="8" maxlength="128" autocomplete="new-password">
                <span class="ayuda">Mínimo 8 caracteres, con letras y números. Deberá cambiarla al entrar.</span>
            </div>

            <label for="nConf">Repetir contraseña</label>
            <input type="password" id="nConf" name="confirmacion" required minlength="8" maxlength="128" autocomplete="new-password">

            <div class="formulario-botones">
                <button type="submit" class="btn btn-primario">Crear usuario</button>
            </div>
        </form>
    </section>
    <% } %>
<%@ include file="/WEB-INF/jspf/pie.jspf" %>
