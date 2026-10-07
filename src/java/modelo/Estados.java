package modelo;

import java.util.List;

/** Estados válidos de una incidencia. Se centralizan aquí para no repetir textos en todo el sistema. */
public final class Estados {

    public static final String ABIERTA = "Abierta";
    public static final String EN_PROCESO = "En proceso";
    public static final String CERRADA = "Cerrada";

    public static final List<String> TODOS = List.of(ABIERTA, EN_PROCESO, CERRADA);

    private Estados() {
    }

    public static boolean esValido(String estado) {
        return estado != null && TODOS.contains(estado);
    }
}
