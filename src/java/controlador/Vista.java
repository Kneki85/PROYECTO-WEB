package controlador;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/** Funciones de apoyo para los JSP: escapar HTML, dar formato a fechas, chips e íconos. */
public final class Vista {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Vista() {
    }

    /** Escapa caracteres especiales de HTML (evita que un texto escrito por un usuario se ejecute como código). */
    public static String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String fecha(Date fecha) {
        return fecha == null ? "" : fecha.toLocalDate().format(FECHA);
    }

    public static String fechaHora(Timestamp momento) {
        return momento == null ? "" : momento.toLocalDateTime().format(FECHA_HORA);
    }

    /** Clase CSS del "chip" de color según el estado. */
    public static String claseEstado(String estado) {
        if (estado == null) return "chip";
        switch (estado) {
            case "Abierta": return "chip chip-abierta";
            case "En proceso": return "chip chip-proceso";
            case "Cerrada": return "chip chip-cerrada";
            default: return "chip";
        }
    }

    public static String icono(String nombre) {
        return icono(nombre, 16);
    }

    /** Íconos SVG simples (estilo Feather, licencia MIT) para no depender de librerías externas. */
    public static String icono(String nombre, int tamano) {
        String cuerpo;
        switch (nombre) {
            case "editar":
                cuerpo = "<path d=\"M12 20h9\"/><path d=\"M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z\"/>";
                break;
            case "historial":
                cuerpo = "<circle cx=\"12\" cy=\"12\" r=\"10\"/><polyline points=\"12 6 12 12 16 14\"/>";
                break;
            case "eliminar":
                cuerpo = "<polyline points=\"3 6 5 6 21 6\"/><path d=\"M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6\"/>"
                        + "<path d=\"M10 11v6\"/><path d=\"M14 11v6\"/><path d=\"M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2\"/>";
                break;
            case "buscar":
                cuerpo = "<circle cx=\"11\" cy=\"11\" r=\"8\"/><line x1=\"21\" y1=\"21\" x2=\"16.65\" y2=\"16.65\"/>";
                break;
            case "usuario":
                cuerpo = "<path d=\"M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2\"/><circle cx=\"12\" cy=\"7\" r=\"4\"/>";
                break;
            case "usuarios":
                cuerpo = "<path d=\"M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2\"/><circle cx=\"9\" cy=\"7\" r=\"4\"/>"
                        + "<path d=\"M23 21v-2a4 4 0 0 0-3-3.87\"/><path d=\"M16 3.13a4 4 0 0 1 0 7.75\"/>";
                break;
            case "usuario-mas":
                cuerpo = "<path d=\"M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2\"/><circle cx=\"8.5\" cy=\"7\" r=\"4\"/>"
                        + "<line x1=\"20\" y1=\"8\" x2=\"20\" y2=\"14\"/><line x1=\"23\" y1=\"11\" x2=\"17\" y2=\"11\"/>";
                break;
            case "clave":
                cuerpo = "<path d=\"M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4\"/>";
                break;
            case "ojo":
                cuerpo = "<path d=\"M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/>";
                break;
            case "mas":
                cuerpo = "<line x1=\"12\" y1=\"5\" x2=\"12\" y2=\"19\"/><line x1=\"5\" y1=\"12\" x2=\"19\" y2=\"12\"/>";
                break;
            case "descargar":
                cuerpo = "<path d=\"M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4\"/><polyline points=\"7 10 12 15 17 10\"/>"
                        + "<line x1=\"12\" y1=\"15\" x2=\"12\" y2=\"3\"/>";
                break;
            case "imprimir":
                cuerpo = "<polyline points=\"6 9 6 2 18 2 18 9\"/>"
                        + "<path d=\"M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2\"/>"
                        + "<rect x=\"6\" y=\"14\" width=\"12\" height=\"8\"/>";
                break;
            case "alerta":
                cuerpo = "<circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" y1=\"8\" x2=\"12\" y2=\"12\"/>"
                        + "<line x1=\"12\" y1=\"16\" x2=\"12.01\" y2=\"16\"/>";
                break;
            default:
                cuerpo = "";
        }
        return "<svg width=\"" + tamano + "\" height=\"" + tamano + "\" viewBox=\"0 0 24 24\" fill=\"none\" "
                + "stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" "
                + "aria-hidden=\"true\" focusable=\"false\">" + cuerpo + "</svg>";
    }
}
