package modelo;

import java.util.List;

/** Un "trozo" (página) de resultados junto con los datos necesarios para paginar. */
public record Pagina<T>(List<T> items, int total, int pagina, int tamano) {

    public int totalPaginas() {
        return Math.max(1, (int) Math.ceil(total / (double) tamano));
    }

    public boolean hayAnterior() {
        return pagina > 1;
    }

    public boolean haySiguiente() {
        return pagina < totalPaginas();
    }
}
