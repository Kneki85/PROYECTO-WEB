package modelo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Criterios de búsqueda de incidencias. Cualquier criterio vacío significa "no filtrar por eso".
 * El mismo objeto se usa en el listado, la paginación y la exportación a CSV.
 */
public class FiltroIncidencias {
    public static final int MAX_TEXTO = 100;

    private String texto = "";
    private String estado = "";
    private Integer idTipo;
    private LocalDate desde;
    private LocalDate hasta;

    public String getTexto() { return texto; }
    public void setTexto(String texto) {
        String t = texto == null ? "" : texto.trim();
        this.texto = t.length() > MAX_TEXTO ? t.substring(0, MAX_TEXTO) : t;
    }

    public String getEstado() { return estado; }
    /** Solo acepta estados válidos; cualquier otro valor (por ejemplo "Todos") equivale a no filtrar. */
    public void setEstado(String estado) {
        this.estado = Estados.esValido(estado) ? estado : "";
    }

    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) {
        this.idTipo = (idTipo != null && idTipo > 0) ? idTipo : null;
    }

    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }

    /** Guarda el rango de fechas; si vienen al revés, las intercambia. */
    public void setRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            LocalDate aux = desde;
            desde = hasta;
            hasta = aux;
        }
        this.desde = desde;
        this.hasta = hasta;
    }

    public boolean hayFiltro() {
        return !texto.isEmpty() || !estado.isEmpty() || idTipo != null || desde != null || hasta != null;
    }

    /** Construye "texto=..&estado=.." (ya codificado para URL) con solo los criterios usados. */
    public String aQueryString() {
        List<String> partes = new ArrayList<>();
        if (!texto.isEmpty()) partes.add("texto=" + codificar(texto));
        if (!estado.isEmpty()) partes.add("estado=" + codificar(estado));
        if (idTipo != null) partes.add("idTipo=" + idTipo);
        if (desde != null) partes.add("desde=" + desde);
        if (hasta != null) partes.add("hasta=" + hasta);
        return String.join("&", partes);
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
