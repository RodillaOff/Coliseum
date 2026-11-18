package servicio;

import config.Conexion;
import dao.ReporteDAO;
import dao.jdbc.ReporteDAOJdbc;
import excepciones.AccesoDatosException;
import excepciones.ColiseumException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import modelo.FilaReporte;
import modelo.RutinaDia;
import modelo.Usuario;

/** Búsquedas y reportes. Un administrador ve a todos los usuarios; un usuario común, solo lo suyo. */
public class ReporteServicio {

    private final ReporteDAO dao;

    public ReporteServicio() {
        this(new ReporteDAOJdbc());
    }

    public ReporteServicio(ReporteDAO dao) {
        this.dao = dao;
    }

    private static Integer alcance(Usuario actor) {
        return actor.puedeAdministrar() ? null : Integer.valueOf(actor.getId());
    }

    public List<RutinaDia> buscar(Usuario actor, String texto, LocalDate desde, LocalDate hasta)
            throws ColiseumException {
        Validador.rango(desde, hasta);
        String filtro = Validador.textoOpcional("Texto a buscar", texto, 100);
        try (Connection con = Conexion.obtener()) {
            return dao.buscar(con, alcance(actor), filtro, desde, hasta);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Buscar notas", e);
        }
    }

    public List<FilaReporte> resumenPorUsuario(Usuario actor) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return dao.resumenPorUsuario(con, alcance(actor));
        } catch (SQLException e) {
            throw AccesoDatosException.de("Reporte por usuario", e);
        }
    }

    public Map<String, Long> notasPorMes(Usuario actor) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return dao.notasPorMes(con, alcance(actor));
        } catch (SQLException e) {
            throw AccesoDatosException.de("Reporte por mes", e);
        }
    }

    public double promedioNotasPorUsuario() throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return dao.promedioNotasPorUsuario(con);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Promedio de notas", e);
        }
    }
}
