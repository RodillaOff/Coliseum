package modelo;

/** Usuario normal: solo accede a sus propias rutinas y reportes. */
public class UsuarioComun extends Usuario {

    public UsuarioComun(int id, String nombre, String hash, Boolean notificaciones) {
        super(id, nombre, hash, notificaciones);
    }

    @Override
    public Rol getRol() {
        return Rol.USUARIO;
    }

    @Override
    public boolean puedeAdministrar() {
        return false;
    }
}
