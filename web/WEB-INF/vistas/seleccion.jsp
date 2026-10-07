<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="controlador.Vista" %>
<%
    String ctx = request.getContextPath();
    String avisoOk = request.getParameter("ok");
    String avisoError = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tipo de personal - GestPersonal</title>
    <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body class="pagina-login">
<main class="seleccion-tarjeta">
    <div class="login-cabecera">
        <div class="login-icono"><%= Vista.icono("usuarios", 28) %></div>
        <h1>GestPersonal</h1>
        <p class="login-sub">Sistema de control de incidencias</p>
    </div>

    <% if (avisoError != null && !avisoError.isEmpty()) { %>
    <div class="aviso aviso-error" role="alert"><%= Vista.icono("alerta") %><span><%= Vista.escapar(avisoError) %></span></div>
    <% } else if (avisoOk != null && !avisoOk.isEmpty()) { %>
    <div class="aviso aviso-ok" role="status"><%= Vista.escapar(avisoOk) %></div>
    <% } %>

    <h2 class="seleccion-titulo">Selecciona tu tipo de personal</h2>

    <div class="seleccion-opciones">
        <a class="opcion-personal" href="<%= ctx %>/LoginServlet?tipo=admin">
            <span class="opcion-icono"><%= Vista.icono("usuarios", 32) %></span>
            <strong>Administrador de personal</strong>
            <span>Registra y edita empleados e incidencias, gestiona usuarios y descarga reportes.</span>
        </a>
        <a class="opcion-personal" href="<%= ctx %>/LoginServlet?tipo=consulta">
            <span class="opcion-icono"><%= Vista.icono("buscar", 32) %></span>
            <strong>Personal de consulta</strong>
            <span>Consulta empleados, incidencias e historial, y descarga la información.</span>
        </a>
    </div>

    <p class="login-pie">Gestión de personal · UNFV · <%= java.time.Year.now() %></p>
</main>
</body>
</html>
