package controlador;

import modelo.FiltroIncidencias;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Funciones de apoyo para leer y validar los parámetros que envía el navegador. */
public final class Peticion {

    private Peticion() {
    }

    /** Convierte un texto en entero; devuelve null si está vacío o no es un número. */
    public static Integer aEntero(String texto) {
        if (texto == null) return null;
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int entero(String texto, int porDefecto) {
        Integer n = aEntero(texto);
        return n == null ? porDefecto : n;
    }

    /** Texto sin espacios al inicio/final; nunca null. */
    public static String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }

    /** Convierte "2026-09-25" en fecha; devuelve null si está vacío o no es válido. */
    public static LocalDate aFecha(String texto) {
        String t = valor(texto);
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Lee los criterios de búsqueda de incidencias (los mismos en listado y exportación). */
    public static FiltroIncidencias filtroIncidencias(HttpServletRequest request) {
        FiltroIncidencias f = new FiltroIncidencias();
        f.setTexto(request.getParameter("texto"));
        f.setEstado(request.getParameter("estado"));
        f.setIdTipo(aEntero(request.getParameter("idTipo")));
        f.setRango(aFecha(request.getParameter("desde")), aFecha(request.getParameter("hasta")));
        return f;
    }
}
