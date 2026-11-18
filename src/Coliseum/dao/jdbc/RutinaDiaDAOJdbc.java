package dao.jdbc;

import dao.RutinaDiaDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import modelo.RutinaDia;

/** Implementación JDBC de RutinaDiaDAO. */
public class RutinaDiaDAOJdbc implements RutinaDiaDAO {

    @Override
    public int agregar(Connection con, int usuarioId, LocalDate fecha, String descripcion) throws SQLException {
        String sql = "INSERT INTO rutinas_dia (usuario_id, fecha, descripcion) VALUES (?, ?, ?)";
        try (PreparedStatement st = con.prepareStatement(sql, new String[] {"id"})) {
            st.setInt(1, usuarioId);
            st.setDate(2, Date.valueOf(fecha));
            st.setString(3, descripcion);
            st.executeUpdate();
            try (ResultSet claves = st.getGeneratedKeys()) {
                return claves.next() ? claves.getInt(1) : -1;
            }
        }
    }

    @Override
    public List<RutinaDia> listarPorFecha(Connection con, int usuarioId, LocalDate fecha) throws SQLException {
        List<RutinaDia> lista = new ArrayList<>();
        String sql = "SELECT id, descripcion FROM rutinas_dia WHERE usuario_id = ? AND fecha = ? ORDER BY id";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, usuarioId);
            st.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RutinaDia(rs.getInt("id"), usuarioId, null, fecha, rs.getString("descripcion")));
                }
            }
        }
        return lista;
    }

    @Override
    public Set<LocalDate> fechasConRutina(Connection con, int usuarioId, YearMonth mes) throws SQLException {
        Set<LocalDate> fechas = new HashSet<>();
        String sql = "SELECT DISTINCT fecha FROM rutinas_dia WHERE usuario_id = ? AND fecha BETWEEN ? AND ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, usuarioId);
            st.setDate(2, Date.valueOf(mes.atDay(1)));
            st.setDate(3, Date.valueOf(mes.atEndOfMonth()));
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    fechas.add(rs.getDate("fecha").toLocalDate());
                }
            }
        }
        return fechas;
    }

    @Override
    public boolean actualizarPorTexto(Connection con, int usuarioId, LocalDate fecha, String textoActual,
                                      String textoNuevo) throws SQLException {
        // PostgreSQL no admite LIMIT en UPDATE: se elige UNA fila (la más vieja) con una subconsulta.
        String sql = "UPDATE rutinas_dia SET descripcion = ? WHERE id = ("
                + "SELECT id FROM rutinas_dia WHERE usuario_id = ? AND fecha = ? AND descripcion = ? ORDER BY id LIMIT 1)";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, textoNuevo);
            st.setInt(2, usuarioId);
            st.setDate(3, Date.valueOf(fecha));
            st.setString(4, textoActual);
            return st.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminarPorTexto(Connection con, int usuarioId, LocalDate fecha, String texto)
            throws SQLException {
        String sql = "DELETE FROM rutinas_dia WHERE id = ("
                + "SELECT id FROM rutinas_dia WHERE usuario_id = ? AND fecha = ? AND descripcion = ? ORDER BY id LIMIT 1)";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, usuarioId);
            st.setDate(2, Date.valueOf(fecha));
            st.setString(3, texto);
            return st.executeUpdate() > 0;
        }
    }
}
