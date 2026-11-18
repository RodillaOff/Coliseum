package modelo;

/** Administrador: además de lo del usuario común, gestiona usuarios y ve el reporte global. */
public class Administrador extends Usuario {

    public Administrador(int id, String nombre, String hash, Boolean notificaciones) {
        super(id, nombre, hash, notificaciones);
    }

    @Override
    public Rol getRol() {
        return Rol.ADMIN;
    }

    @Override
    public boolean puedeAdministrar() {
        return true;
    }
}
