package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import modelo.FilaReporte;
import modelo.RutinaDia;

/** Consultas de lectura para búsquedas y reportes (R6). Si usuarioId es null, abarca a todos los usuarios. */
public interface ReporteDAO {
    /** Búsqueda con filtros opcionales (texto, desde, hasta) y JOIN con usuarios. */
    List<RutinaDia> buscar(Connection con, Integer usuarioId, String texto, LocalDate desde, LocalDate hasta)
            throws SQLException;

    /** Reporte por usuario: JOIN + COUNT + GROUP BY. */
    List<FilaReporte> resumenPorUsuario(Connection con, Integer usuarioId) throws SQLException;

    /** Cantidad de notas por mes (COUNT + GROUP BY). Clave: "yyyy-MM". */
    Map<String, Long> notasPorMes(Connection con, Integer usuarioId) throws SQLException;

    /** Promedio de notas por usuario (AVG sobre una subconsulta con GROUP BY). */
    double promedioNotasPorUsuario(Connection con) throws SQLException;
}
