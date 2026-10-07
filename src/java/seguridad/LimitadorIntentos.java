package seguridad;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Frena los intentos repetidos de adivinar una contraseña: tras varios fallos seguidos
 * bloquea temporalmente esa combinación usuario + dirección IP. Vive en memoria
 * (se reinicia al reiniciar el servidor), que es suficiente para esta aplicación.
 */
public final class LimitadorIntentos {

    private static final int MAX_FALLOS = 5;
    private static final long VENTANA_MS = 10 * 60 * 1000L;  // los fallos cuentan durante 10 minutos
    private static final long BLOQUEO_MS = 5 * 60 * 1000L;   // duración del bloqueo
    private static final int TAMANO_MAXIMO = 5000;

    private static final class Registro {
        int fallos;
        long primerFallo;
        long bloqueadoHasta;
    }

    private static final Map<String, Registro> REGISTROS = new HashMap<>();

    private LimitadorIntentos() {
    }

    /** Segundos que faltan para poder intentar de nuevo; 0 si no está bloqueado. */
    public static synchronized long segundosDeBloqueo(String clave) {
        Registro r = REGISTROS.get(clave);
        if (r == null) return 0;
        long restante = r.bloqueadoHasta - System.currentTimeMillis();
        return restante > 0 ? (restante + 999) / 1000 : 0;
    }

    public static synchronized void registrarFallo(String clave) {
        long ahora = System.currentTimeMillis();
        if (REGISTROS.size() > TAMANO_MAXIMO) purgar(ahora);
        Registro r = REGISTROS.computeIfAbsent(clave, k -> new Registro());
        if (ahora - r.primerFallo > VENTANA_MS) {
            r.fallos = 0;
            r.primerFallo = ahora;
        }
        r.fallos++;
        if (r.fallos >= MAX_FALLOS) {
            r.bloqueadoHasta = ahora + BLOQUEO_MS;
            r.fallos = 0;
            r.primerFallo = ahora;
        }
    }

    public static synchronized void limpiar(String clave) {
        REGISTROS.remove(clave);
    }

    private static void purgar(long ahora) {
        Iterator<Map.Entry<String, Registro>> it = REGISTROS.entrySet().iterator();
        while (it.hasNext()) {
            Registro r = it.next().getValue();
            if (r.bloqueadoHasta < ahora && ahora - r.primerFallo > VENTANA_MS) it.remove();
        }
    }
}
