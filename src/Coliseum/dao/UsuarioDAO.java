package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import modelo.Rol;
import modelo.Usuario;

/** Acceso a datos de la tabla usuarios. Cada método recibe la conexión para poder usarse dentro de una transacción. */
public interface UsuarioDAO {
    int crear(Connection con, Usuario usuario) throws SQLException;

    Optional<Usuario> buscarPorNombre(Connection con, String nombre) throws SQLException;

    Optional<Usuario> buscarPorId(Connection con, int id) throws SQLException;

    List<Usuario> listar(Connection con) throws SQLException;

    boolean actualizarNombre(Connection con, int id, String nombre) throws SQLException;

    boolean actualizarHash(Connection con, int id, String hash) throws SQLException;

    boolean actualizarRol(Connection con, int id, Rol rol) throws SQLException;

    boolean actualizarNotificaciones(Connection con, int id, boolean activas) throws SQLException;

    boolean eliminar(Connection con, int id) throws SQLException;

    int contarPorRol(Connection con, Rol rol) throws SQLException;
}
