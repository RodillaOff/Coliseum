package dao.jdbc;

import dao.ReporteDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.FilaReporte;
import modelo.Rol;
import modelo.RutinaDia;

/** Implementación JDBC de ReporteDAO (R6). Los filtros opcionales se agregan con parámetros "?" (R9). */
public class ReporteDAOJdbc implements ReporteDAO {

    @Override
    public List<RutinaDia> buscar(Connection con, Integer usuarioId, String texto, LocalDate desde, LocalDate hasta)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.usuario_id, u.nombre_usuario, r.fecha, r.descripcion "
                        + "FROM rutinas_dia r JOIN usuarios u ON u.id = r.usuario_id WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (usuarioId != null) {
            sql.append(" AND r.usuario_id = ?");
            parametros.add(usuarioId);
        }
        if (texto != null && !texto.isEmpty()) {
            sql.append(" AND r.descripcion ILIKE ?"); // ILIKE: sin distinguir mayúsculas (como LIKE en MySQL)
            parametros.add("%" + texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        if (desde != null) {
            sql.append(" AND r.fecha >= ?");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append(" AND r.fecha <= ?");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append(" ORDER BY r.fecha DESC, r.id DESC LIMIT 200");

        List<RutinaDia> resultado = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                st.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new RutinaDia(rs.getInt("id"), rs.getInt("usuario_id"),
                            rs.getString("nombre_usuario"), rs.getDate("fecha").toLocalDate(),
                            rs.getString("descripcion")));
                }
            }
        }
        return resultado;
    }

    @Override
    public List<FilaReporte> resumenPorUsuario(Connection con, Integer usuarioId) throws SQLException {
        String sql = "SELECT u.nombre_usuario, u.rol, COUNT(DISTINCT rd.id) AS notas, "
                + "COUNT(DISTINCT rs.dia_semana) AS dias_configurados "
                + "FROM usuarios u "
                + "LEFT JOIN rutinas_dia rd ON rd.usuario_id = u.id "
                + "LEFT JOIN rutina_semanal rs ON rs.usuario_id = u.id "
                + (usuarioId != null ? "WHERE u.id = ? " : "")
                + "GROUP BY u.id, u.nombre_usuario, u.rol ORDER BY notas DESC, u.nombre_usuario";
        List<FilaReporte> filas = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            if (usuarioId != null) {
                st.setInt(1, usuarioId);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    filas.add(new FilaReporte(rs.getString("nombre_usuario"), Rol.desde(rs.getString("rol")),
                            rs.getLong("notas"), rs.getLong("dias_configurados")));
                }
            }
        }
        return filas;
    }

    @Override
    public Map<String, Long> notasPorMes(Connection con, Integer usuarioId) throws SQLException {
        String sql = "SELECT to_char(fecha, 'YYYY-MM') AS mes, COUNT(*) AS cantidad FROM rutinas_dia "
                + (usuarioId != null ? "WHERE usuario_id = ? " : "")
                + "GROUP BY to_char(fecha, 'YYYY-MM') ORDER BY mes";
        Map<String, Long> meses = new LinkedHashMap<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            if (usuarioId != null) {
                st.setInt(1, usuarioId);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    meses.put(rs.getString("mes"), rs.getLong("cantidad"));
                }
            }
        }
        return meses;
    }

    @Override
    public double promedioNotasPorUsuario(Connection con) throws SQLException {
        String sql = "SELECT AVG(t.cantidad) FROM ("
                + "SELECT u.id, COUNT(rd.id) AS cantidad FROM usuarios u "
                + "LEFT JOIN rutinas_dia rd ON rd.usuario_id = u.id GROUP BY u.id) t";
        try (PreparedStatement st = con.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
}
