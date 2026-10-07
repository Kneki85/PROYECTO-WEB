<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="controlador.Vista, seguridad.Seguridad, modelo.Rol" %>
<%
    String ctx = request.getContextPath();
    Rol tipoRol = (Rol) request.getAttribute("tipoRol");
    if (tipoRol == null) { response.sendRedirect(ctx + "/SeleccionServlet"); return; }
    String tipoClave = tipoRol == Rol.ADMIN ? "admin" : "consulta";
    String errorLogin = (String) request.getAttribute("errorLogin");
    String usuarioIngresado = (String) request.getAttribute("usuarioIngresado");
    if (usuarioIngresado == null) usuarioIngresado = "";
    boolean conError = errorLogin != null;
    String avisoOk = request.getParameter("ok");
    String avisoParam = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión - GestPersonal</title>
    <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body class="pagina-login">
<main class="login-tarjeta">
    <div class="login-cabecera">
        <div class="login-icono"><%= Vista.icono("usuarios", 28) %></div>
        <h1>GestPersonal</h1>
        <p class="login-sub">Sistema de control de incidencias</p>
        <span class="chip chip-tipo login-tipo"><%= Vista.escapar(tipoRol.getEtiqueta()) %></span>
    </div>

    <% if (conError) { %>
    <div class="aviso aviso-error" role="alert"><%= Vista.icono("alerta") %><span><%= Vista.escapar(errorLogin) %></span></div>
    <% } else if (avisoParam != null && !avisoParam.isEmpty()) { %>
    <div class="aviso aviso-error" role="alert"><%= Vista.icono("alerta") %><span><%= Vista.escapar(avisoParam) %></span></div>
    <% } else if (avisoOk != null && !avisoOk.isEmpty()) { %>
    <div class="aviso aviso-ok" role="status"><%= Vista.escapar(avisoOk) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/LoginServlet" id="formLogin">
        <%= Seguridad.campoCsrf(request) %>
        <input type="hidden" name="accion" value="entrar">
        <input type="hidden" name="tipo" value="<%= tipoClave %>">

        <label for="usuario">Usuario</label>
        <div class="campo <%= conError ? "campo-error" : "" %>">
            <%= Vista.icono("usuario") %>
            <input type="text" id="usuario" name="usuario" required maxlength="50" autofocus
                   autocomplete="username" value="<%= Vista.escapar(usuarioIngresado) %>">
        </div>

        <label for="clave">Contraseña</label>
        <div class="campo <%= conError ? "campo-error" : "" %>">
            <%= Vista.icono("clave") %>
            <input type="password" id="clave" name="clave" required maxlength="128" autocomplete="current-password">
            <button type="button" class="campo-boton" id="verClave" aria-label="Mostrar u ocultar la contraseña"><%= Vista.icono("ojo") %></button>
        </div>

        <button type="submit" class="btn btn-primario btn-bloque" id="btnEntrar">Ingresar</button>
    </form>

    <p class="login-cambiar"><a href="<%= ctx %>/SeleccionServlet">&larr; Cambiar tipo de personal</a></p>

    <p class="login-pie">Gestión de personal · UNFV · <%= java.time.Year.now() %></p>
</main>

<script>
    (function () {
        var clave = document.getElementById('clave');
        var boton = document.getElementById('btnEntrar');
        var form = document.getElementById('formLogin');

        document.getElementById('verClave').addEventListener('click', function () {
            clave.type = clave.type === 'password' ? 'text' : 'password';
        });

        // Evita enviar dos veces el formulario y muestra que se está verificando
        form.addEventListener('submit', function () {
            boton.disabled = true;
            boton.textContent = 'Verificando…';
        });
        // Si el usuario vuelve con el botón "Atrás", se restablece el botón
        window.addEventListener('pageshow', function () {
            boton.disabled = false;
            boton.textContent = 'Ingresar';
        });
    })();
</script>
</body>
</html>
