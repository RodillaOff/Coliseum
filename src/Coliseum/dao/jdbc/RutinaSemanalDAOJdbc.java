package dao.jdbc;

import dao.RutinaSemanalDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import modelo.RutinaSemanal;

/** Implementación JDBC de RutinaSemanalDAO. */
public class RutinaSemanalDAOJdbc implements RutinaSemanalDAO {

    @Override
    public void guardarDia(Connection con, int usuarioId, DayOfWeek dia, String resumen, String horaSql)
            throws SQLException {
        String sql = "INSERT INTO rutina_semanal (usuario_id, dia_semana, resumen, ejercicios, hora) "
                + "VALUES (?, ?, ?, '', CAST(? AS TIME)) "
                + "ON CONFLICT (usuario_id, dia_semana) DO UPDATE SET resumen = EXCLUDED.resumen, hora = EXCLUDED.hora";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, usuarioId);
            st.setString(2, dia.name());
            st.setString(3, resumen);
            if (horaSql == null) {
                st.setNull(4, Types.VARCHAR);
            } else {
                st.setString(4, horaSql);
            }
            st.executeUpdate();
        }
    }

    @Override
    public void guardarEjercicios(Connection con, int usuarioId, DayOfWeek dia, String ejercicios)
            throws SQLException {
        String sql = "INSERT INTO rutina_semanal (usuario_id, dia_semana, resumen, ejercicios) "
                + "VALUES (?, ?, '', ?) "
                + "ON CONFLICT (usuario_id, dia_semana) DO UPDATE SET ejercicios = EXCLUDED.ejercicios";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, usuarioId);
            st.setString(2, dia.name());
            st.setString(3, ejercicios);
            st.executeUpdate();
        }
    }

    @Override
    public List<RutinaSemanal> listar(Connection con, int usuarioId) throws SQLException {
        List<RutinaSemanal> lista = new ArrayList<>();
        String sql = "SELECT dia_semana, resumen, ejercicios, hora FROM rutina_semanal WHERE usuario_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, usuarioId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String hora = rs.getString("hora"); // como texto, para evitar conversiones de zona horaria
                    String ejercicios = rs.getString("ejercicios");
                    lista.add(new RutinaSemanal(usuarioId, DayOfWeek.valueOf(rs.getString("dia_semana")),
                            rs.getString("resumen"), ejercicios == null ? "" : ejercicios,
                            hora == null || hora.trim().isEmpty() ? null : LocalTime.parse(hora)));
                }
            }
        }
        return lista;
    }

    @Override
    public boolean eliminarDia(Connection con, int usuarioId, DayOfWeek dia) throws SQLException {
        try (PreparedStatement st = con.prepareStatement(
                "DELETE FROM rutina_semanal WHERE usuario_id = ? AND dia_semana = ?")) {
            st.setInt(1, usuarioId);
            st.setString(2, dia.name());
            return st.executeUpdate() > 0;
        }
    }
}
