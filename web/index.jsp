<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // La página de inicio real la arma InicioServlet (necesita consultar la base de datos).
    // Si no hay sesión iniciada, el filtro de seguridad enviará al login.
    response.sendRedirect(request.getContextPath() + "/InicioServlet");
%>
