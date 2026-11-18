package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import modelo.RutinaDia;

/** Acceso a datos de rutinas_dia: alta, consulta, modificación y baja de notas puntuales. */
public interface RutinaDiaDAO {
    int agregar(Connection con, int usuarioId, LocalDate fecha, String descripcion) throws SQLException;

    List<RutinaDia> listarPorFecha(Connection con, int usuarioId, LocalDate fecha) throws SQLException;

    Set<LocalDate> fechasConRutina(Connection con, int usuarioId, YearMonth mes) throws SQLException;

    boolean actualizarPorTexto(Connection con, int usuarioId, LocalDate fecha, String textoActual, String textoNuevo)
            throws SQLException;

    boolean eliminarPorTexto(Connection con, int usuarioId, LocalDate fecha, String texto) throws SQLException;
}
