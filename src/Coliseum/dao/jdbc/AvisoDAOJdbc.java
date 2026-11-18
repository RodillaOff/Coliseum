package dao.jdbc;

import dao.AvisoDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/** Implementación JDBC de AvisoDAO. */
public class AvisoDAOJdbc implements AvisoDAO {

    @Override
    public void limpiarDesde(Connection con, int usuarioId, LocalDate desde) throws SQLException {
        try (PreparedStatement st = con.prepareStatement(
                "DELETE FROM avisos_entreno WHERE usuario_id = ? AND fecha >= ?")) {
            st.setInt(1, usuarioId);
            st.setDate(2, Date.valueOf(desde));
            st.executeUpdate();
        }
    }
}
