package modelo;

/**
 * Raíz de la jerarquía de herencia del dominio (R5): Usuario -> UsuarioComun / Administrador.
 * Cada subclase define su rol y qué puede hacer (polimorfismo).
 */
public abstract class Usuario {

    private final int id;
    private final String nombre;
    private final String hashContrasena;
    private final Boolean notificaciones; // null = todavía no eligió

    protected Usuario(int id, String nombre, String hashContrasena, Boolean notificaciones) {
        this.id = id;
        this.nombre = nombre;
        this.hashContrasena = hashContrasena;
        this.notificaciones = notificaciones;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public Boolean getNotificaciones() {
        return notificaciones;
    }

    public abstract Rol getRol();

    /** Si puede gestionar a otros usuarios y ver reportes de todos. */
    public abstract boolean puedeAdministrar();

    /** Fábrica: devuelve la subclase que corresponde al rol. */
    public static Usuario crear(Rol rol, int id, String nombre, String hash, Boolean notificaciones) {
        if (rol == Rol.ADMIN) {
            return new Administrador(id, nombre, hash, notificaciones);
        }
        return new UsuarioComun(id, nombre, hash, notificaciones);
    }
}
