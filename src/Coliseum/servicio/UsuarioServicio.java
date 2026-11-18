package servicio;

import config.Conexion;
import dao.UsuarioDAO;
import dao.jdbc.UsuarioDAOJdbc;
import excepciones.AccesoDatosException;
import excepciones.AutorizacionException;
import excepciones.ColiseumException;
import excepciones.ValidacionException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import modelo.Rol;
import modelo.Usuario;
import seguridad.Hashes;

/** Lógica de usuarios, sesión y roles. Depende de la interfaz UsuarioDAO, no de JDBC. */
public class UsuarioServicio {

    private final UsuarioDAO dao;

    public UsuarioServicio() {
        this(new UsuarioDAOJdbc());
    }

    public UsuarioServicio(UsuarioDAO dao) {
        this.dao = dao;
    }

    /** Alta de usuario (rol USUARIO). Devuelve false si el nombre ya está en uso. */
    public boolean registrar(String nombre, String contrasena) throws ColiseumException {
        String n = Validador.nombreUsuario(nombre);
        Validador.contrasena(contrasena);
        try (Connection con = Conexion.obtener()) {
            if (dao.buscarPorNombre(con, n).isPresent()) {
                return false;
            }
            dao.crear(con, Usuario.crear(Rol.USUARIO, 0, n, Hashes.actual().hashear(contrasena), null));
            return true;
        } catch (SQLException e) {
            if (esNombreRepetido(e)) {
                return false; // otro registro simultáneo se quedó con el nombre
            }
            throw AccesoDatosException.de("Registrar usuario", e);
        }
    }

    /** Inicio de sesión. Si la cuenta tenía el hash viejo (SHA-256), lo actualiza al algoritmo actual. */
    public Optional<Usuario> autenticar(String nombre, String contrasena) throws ColiseumException {
        if (nombre == null || nombre.trim().isEmpty() || contrasena == null || contrasena.isEmpty()) {
            return Optional.empty();
        }
        try (Connection con = Conexion.obtener()) {
            Optional<Usuario> encontrado = dao.buscarPorNombre(con, nombre.trim());
            if (!encontrado.isPresent()) {
                return Optional.empty();
            }
            Usuario u = encontrado.get();
            if (!Hashes.verificar(contrasena, u.getHashContrasena())) {
                return Optional.empty();
            }
            if (Hashes.necesitaActualizar(u.getHashContrasena())) {
                dao.actualizarHash(con, u.getId(), Hashes.actual().hashear(contrasena));
            }
            return encontrado;
        } catch (SQLException e) {
            throw AccesoDatosException.de("Autenticar", e);
        }
    }

    public Optional<Usuario> buscarPorNombre(String nombre) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return dao.buscarPorNombre(con, nombre);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Buscar usuario", e);
        }
    }

    public Optional<Usuario> buscarPorId(int id) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return dao.buscarPorId(con, id);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Buscar usuario por id", e);
        }
    }

    // ---- Modificación de la propia cuenta ----

    public void cambiarContrasena(int id, String actual, String nueva) throws ColiseumException {
        Validador.contrasena(nueva);
        try (Connection con = Conexion.obtener()) {
            Usuario u = exigirContrasena(con, id, actual);
            dao.actualizarHash(con, u.getId(), Hashes.actual().hashear(nueva));
        } catch (SQLException e) {
            throw AccesoDatosException.de("Cambiar contraseña", e);
        }
    }

    public void renombrar(int id, String contrasena, String nuevoNombre) throws ColiseumException {
        String n = Validador.nombreUsuario(nuevoNombre);
        try (Connection con = Conexion.obtener()) {
            exigirContrasena(con, id, contrasena);
            if (dao.buscarPorNombre(con, n).isPresent()) {
                throw new ValidacionException("Ese nombre de usuario ya existe.");
            }
            dao.actualizarNombre(con, id, n);
        } catch (SQLException e) {
            if (esNombreRepetido(e)) {
                throw new ValidacionException("Ese nombre de usuario ya existe.");
            }
            throw AccesoDatosException.de("Renombrar usuario", e);
        }
    }

    /** Baja de la propia cuenta (borra también sus rutinas por ON DELETE CASCADE). */
    public void eliminarCuenta(int id, String contrasena) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            Usuario u = exigirContrasena(con, id, contrasena);
            exigirQueNoSeaElUltimoAdmin(con, u);
            dao.eliminar(con, id);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Eliminar cuenta", e);
        }
    }

    // ---- Notificaciones ----

    public Boolean obtenerNotificaciones(int id) throws ColiseumException {
        return buscarPorId(id).map(Usuario::getNotificaciones).orElse(null);
    }

    public void guardarNotificaciones(int id, boolean activas) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            dao.actualizarNotificaciones(con, id, activas);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Guardar notificaciones", e);
        }
    }

    // ---- Operaciones de administrador (R3: permisos distintos por rol) ----

    public List<Usuario> listar(Usuario actor) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            exigirAdmin(con, actor);
            return dao.listar(con);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Listar usuarios", e);
        }
    }

    public void cambiarRol(Usuario actor, int idObjetivo, Rol nuevoRol) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            exigirAdmin(con, actor);
            Usuario objetivo = dao.buscarPorId(con, idObjetivo)
                    .orElseThrow(() -> new ValidacionException("El usuario ya no existe."));
            if (objetivo.getRol() == Rol.ADMIN && nuevoRol != Rol.ADMIN) {
                exigirQueNoSeaElUltimoAdmin(con, objetivo);
            }
            dao.actualizarRol(con, idObjetivo, nuevoRol);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Cambiar rol", e);
        }
    }

    public void restablecerContrasena(Usuario actor, int idObjetivo, String nueva) throws ColiseumException {
        Validador.contrasena(nueva);
        try (Connection con = Conexion.obtener()) {
            exigirAdmin(con, actor);
            dao.actualizarHash(con, idObjetivo, Hashes.actual().hashear(nueva));
        } catch (SQLException e) {
            throw AccesoDatosException.de("Restablecer contraseña", e);
        }
    }

    public void eliminarUsuario(Usuario actor, int idObjetivo) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            exigirAdmin(con, actor);
            Usuario objetivo = dao.buscarPorId(con, idObjetivo)
                    .orElseThrow(() -> new ValidacionException("El usuario ya no existe."));
            exigirQueNoSeaElUltimoAdmin(con, objetivo);
            dao.eliminar(con, idObjetivo);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Eliminar usuario", e);
        }
    }

    // ---- Reglas internas ----

    /** Vuelve a leer el rol desde la base: no se confía en el objeto que mantiene la interfaz. */
    private void exigirAdmin(Connection con, Usuario actor) throws SQLException, ColiseumException {
        Optional<Usuario> real = actor == null ? Optional.<Usuario>empty() : dao.buscarPorId(con, actor.getId());
        if (!real.isPresent() || !real.get().puedeAdministrar()) {
            throw new AutorizacionException("Esta acción es solo para administradores.");
        }
    }

    private Usuario exigirContrasena(Connection con, int id, String contrasena)
            throws SQLException, ColiseumException {
        Usuario u = dao.buscarPorId(con, id).orElseThrow(() -> new ValidacionException("El usuario ya no existe."));
        if (contrasena == null || !Hashes.verificar(contrasena, u.getHashContrasena())) {
            throw new ValidacionException("La contraseña actual no es correcta.");
        }
        return u;
    }

    private void exigirQueNoSeaElUltimoAdmin(Connection con, Usuario u) throws SQLException, ValidacionException {
        if (u.getRol() == Rol.ADMIN && dao.contarPorRol(con, Rol.ADMIN) <= 1) {
            throw new ValidacionException("No se puede dejar el sistema sin ningún administrador.");
        }
    }

    /** SQLSTATE 23505 = se violó una clave única (en este caso, nombre_usuario repetido). */
    private static boolean esNombreRepetido(SQLException e) {
        return "23505".equals(e.getSQLState());
    }
}
