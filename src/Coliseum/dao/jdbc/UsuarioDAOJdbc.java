package dao.jdbc;

import dao.UsuarioDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import modelo.Rol;
import modelo.Usuario;

/** Implementación JDBC de UsuarioDAO. Todas las consultas usan PreparedStatement (R9). */
public class UsuarioDAOJdbc implements UsuarioDAO {

    private static final String COLUMNAS = "id, nombre_usuario, contrasena_hash, rol, notificaciones";

    @Override
    public int crear(Connection con, Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre_usuario, contrasena_hash, rol) VALUES (?, ?, ?)";
        try (PreparedStatement st = con.prepareStatement(sql, new String[] {"id"})) {
            st.setString(1, u.getNombre());
            st.setString(2, u.getHashContrasena());
            st.setString(3, u.getRol().name());
            st.executeUpdate();
            try (ResultSet claves = st.getGeneratedKeys()) {
                return claves.next() ? claves.getInt(1) : -1;
            }
        }
    }

    @Override
    public Optional<Usuario> buscarPorNombre(Connection con, String nombre) throws SQLException {
        try (PreparedStatement st = con.prepareStatement(
                "SELECT " + COLUMNAS + " FROM usuarios WHERE nombre_usuario = ?")) {
            st.setString(1, nombre);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.<Usuario>empty();
            }
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Connection con, int id) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("SELECT " + COLUMNAS + " FROM usuarios WHERE id = ?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.<Usuario>empty();
            }
        }
    }

    @Override
    public List<Usuario> listar(Connection con) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(
                "SELECT " + COLUMNAS + " FROM usuarios ORDER BY nombre_usuario");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public boolean actualizarNombre(Connection con, int id, String nombre) throws SQLException {
        return actualizar(con, "UPDATE usuarios SET nombre_usuario = ? WHERE id = ?", nombre, id);
    }

    @Override
    public boolean actualizarHash(Connection con, int id, String hash) throws SQLException {
        return actualizar(con, "UPDATE usuarios SET contrasena_hash = ? WHERE id = ?", hash, id);
    }

    @Override
    public boolean actualizarRol(Connection con, int id, Rol rol) throws SQLException {
        return actualizar(con, "UPDATE usuarios SET rol = ? WHERE id = ?", rol.name(), id);
    }

    @Override
    public boolean actualizarNotificaciones(Connection con, int id, boolean activas) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("UPDATE usuarios SET notificaciones = ? WHERE id = ?")) {
            st.setInt(1, activas ? 1 : 0);
            st.setInt(2, id);
            return st.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(Connection con, int id) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        }
    }

    @Override
    public int contarPorRol(Connection con, Rol rol) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE rol = ?")) {
            st.setString(1, rol.name());
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private boolean actualizar(Connection con, String sql, String valor, int id) throws SQLException {
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, valor);
            st.setInt(2, id);
            return st.executeUpdate() > 0;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        int valor = rs.getInt("notificaciones");
        Boolean notificaciones = rs.wasNull() ? null : Boolean.valueOf(valor == 1);
        return Usuario.crear(Rol.desde(rs.getString("rol")), rs.getInt("id"), rs.getString("nombre_usuario"),
                rs.getString("contrasena_hash"), notificaciones);
    }
}
