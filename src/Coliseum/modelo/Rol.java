package modelo;

/** Roles del sistema (R3). ADMIN gestiona usuarios y ve reportes globales; USUARIO solo sus datos. */
public enum Rol {
    ADMIN("Administrador"),
    USUARIO("Usuario");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Rol desde(String texto) {
        return "ADMIN".equalsIgnoreCase(texto) ? ADMIN : USUARIO;
    }
}
