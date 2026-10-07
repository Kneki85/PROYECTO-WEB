package modelo;

/**
 * Roles del sistema.
 * ADMIN: puede crear, editar y eliminar todo, y administrar usuarios.
 * CONSULTA: solo puede ver información y descargarla.
 */
public enum Rol {
    ADMIN("Administrador de personal"),
    CONSULTA("Personal de consulta");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Convierte un texto en rol; devuelve null si no corresponde a ninguno. */
    public static Rol desde(String texto) {
        if (texto == null) return null;
        try {
            return Rol.valueOf(texto.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
