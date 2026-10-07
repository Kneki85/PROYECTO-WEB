<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ page import="controlador.Vista" %>
<%
    String ctx = request.getContextPath();
    Integer codigo = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    Throwable causa = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

    // ¿El problema viene de la base de datos? (los DAO siempre incluyen "base de datos" en su mensaje)
    boolean esBD = false;
    for (Throwable t = causa; t != null && !esBD; t = t.getCause()) {
        esBD = t instanceof datos.DatosException
                || (t.getMessage() != null && t.getMessage().toLowerCase().contains("base de datos"));
    }

    String titulo;
    String detalle;
    if (esBD) {
        titulo = "No se pudo acceder a la base de datos";
        detalle = "Verifica que MySQL esté iniciado en XAMPP, que la base «incidencias_db» exista y que los datos de conexión sean correctos.";
    } else if (codigo != null && codigo == 403) {
        titulo = "Acceso denegado";
        detalle = "No tienes permiso para realizar esta acción, o la página caducó. Vuelve al inicio e inténtalo de nuevo.";
    } else if (codigo != null && codigo == 404) {
        titulo = "Página no encontrada";
        detalle = "La dirección que abriste no existe.";
    } else if (codigo != null && codigo == 400) {
        titulo = "Solicitud no válida";
        detalle = "La solicitud enviada no es correcta.";
    } else {
        titulo = "Ocurrió un error inesperado";
        detalle = "No se pudo completar la operación. Si el problema continúa, avisa al administrador del sistema.";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Error - GestPersonal</title>
    <link rel="stylesheet" href="<%= ctx %>/css/estilos.css">
</head>
<body>
<div class="contenedor">
    <header class="encabezado">
        <div class="encabezado-fila"><h1 class="marca">Control de incidencias</h1></div>
    </header>
    <section class="tarjeta">
        <h2><%= Vista.escapar(titulo) %></h2>
        <p><%= Vista.escapar(detalle) %></p>
        <a class="btn btn-primario" href="<%= ctx %>/InicioServlet">Volver al inicio</a>
    </section>
</div>
</body>
</html>
