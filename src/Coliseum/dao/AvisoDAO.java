package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/** Acceso a datos de avisos_entreno (qué avisos ya se enviaron). */
public interface AvisoDAO {
    /** Borra los avisos de esa fecha en adelante, para que vuelvan a estar disponibles. */
    void limpiarDesde(Connection con, int usuarioId, LocalDate desde) throws SQLException;
}
