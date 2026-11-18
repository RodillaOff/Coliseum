package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.List;
import modelo.RutinaSemanal;

/** Acceso a datos de rutina_semanal (alta/modificación con upsert, consulta y baja). */
public interface RutinaSemanalDAO {
    /** Crea o actualiza el resumen y la hora (formato HH:mm:ss, o null) de un día. */
    void guardarDia(Connection con, int usuarioId, DayOfWeek dia, String resumen, String horaSql) throws SQLException;

    void guardarEjercicios(Connection con, int usuarioId, DayOfWeek dia, String ejercicios) throws SQLException;

    List<RutinaSemanal> listar(Connection con, int usuarioId) throws SQLException;

    boolean eliminarDia(Connection con, int usuarioId, DayOfWeek dia) throws SQLException;
}
