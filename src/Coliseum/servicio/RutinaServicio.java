package servicio;

import config.Conexion;
import dao.AvisoDAO;
import dao.RutinaDiaDAO;
import dao.RutinaSemanalDAO;
import dao.jdbc.AvisoDAOJdbc;
import dao.jdbc.RutinaDiaDAOJdbc;
import dao.jdbc.RutinaSemanalDAOJdbc;
import excepciones.AccesoDatosException;
import excepciones.ColiseumException;
import excepciones.ValidacionException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import modelo.RutinaDia;
import modelo.RutinaSemanal;

/** Lógica de la rutina semanal y de las notas por fecha. */
public class RutinaServicio {

    private static final int MAX_RESUMEN = 255;
    private static final int MAX_EJERCICIOS = 20000;
    private static final int MAX_NOTA = 255;

    private final RutinaSemanalDAO semanalDao;
    private final RutinaDiaDAO diaDao;
    private final AvisoDAO avisoDao;

    public RutinaServicio() {
        this(new RutinaSemanalDAOJdbc(), new RutinaDiaDAOJdbc(), new AvisoDAOJdbc());
    }

    public RutinaServicio(RutinaSemanalDAO semanalDao, RutinaDiaDAO diaDao, AvisoDAO avisoDao) {
        this.semanalDao = semanalDao;
        this.diaDao = diaDao;
        this.avisoDao = avisoDao;
    }

    /**
     * R7: TRANSACCIÓN sobre dos tablas. Guarda toda la semana en rutina_semanal y borra los avisos
     * ya enviados en avisos_entreno. Si cualquier paso falla se hace rollback y no queda nada a medias.
     */
    public void guardarSemana(int usuarioId, Map<DayOfWeek, String> resumenes, Map<DayOfWeek, String> horas)
            throws ColiseumException {
        // Primero se valida todo, antes de abrir la conexión.
        List<Object[]> filas = new ArrayList<>();
        for (Map.Entry<DayOfWeek, String> e : resumenes.entrySet()) {
            String resumen = Validador.textoOpcional("Resumen de " + e.getKey(), e.getValue(), MAX_RESUMEN);
            String hora = Validador.hora(horas.get(e.getKey()));
            filas.add(new Object[]{e.getKey(), resumen, hora.isEmpty() ? null : hora + ":00"});
        }

        Connection con = Conexion.obtener();
        try {
            con.setAutoCommit(false);
            for (Object[] f : filas) {
                semanalDao.guardarDia(con, usuarioId, (DayOfWeek) f[0], (String) f[1], (String) f[2]);
            }
            avisoDao.limpiarDesde(con, usuarioId, LocalDate.now());
            con.commit();
        } catch (SQLException e) {
            Conexiones.revertir(con);
            throw AccesoDatosException.de("Guardar rutina semanal (rollback aplicado)", e);
        } finally {
            Conexiones.cerrar(con);
        }
    }

    public List<RutinaSemanal> cargarSemana(int usuarioId) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return semanalDao.listar(con, usuarioId);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Cargar rutina semanal", e);
        }
    }

    public void guardarEjercicios(int usuarioId, DayOfWeek dia, String ejercicios) throws ColiseumException {
        String texto = Validador.textoOpcional("Ejercicios", ejercicios, MAX_EJERCICIOS);
        try (Connection con = Conexion.obtener()) {
            semanalDao.guardarEjercicios(con, usuarioId, dia, texto);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Guardar ejercicios", e);
        }
    }

    /** Baja de la rutina habitual de un día. */
    public boolean eliminarDiaSemanal(int usuarioId, DayOfWeek dia) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return semanalDao.eliminarDia(con, usuarioId, dia);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Eliminar día de la rutina semanal", e);
        }
    }

    // ---- Notas por fecha: alta, consulta, modificación y baja ----

    public void agregarNota(int usuarioId, LocalDate fecha, String descripcion) throws ColiseumException {
        Validador.fecha(fecha);
        String texto = Validador.texto("Nota", descripcion, MAX_NOTA);
        try (Connection con = Conexion.obtener()) {
            diaDao.agregar(con, usuarioId, fecha, texto);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Agregar nota", e);
        }
    }

    public List<String> listarNotas(int usuarioId, LocalDate fecha) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            List<String> textos = new ArrayList<>();
            for (RutinaDia r : diaDao.listarPorFecha(con, usuarioId, fecha)) {
                textos.add(r.getDescripcion());
            }
            return textos;
        } catch (SQLException e) {
            throw AccesoDatosException.de("Listar notas", e);
        }
    }

    public Set<LocalDate> fechasConNotas(int usuarioId, YearMonth mes) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            return diaDao.fechasConRutina(con, usuarioId, mes);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Cargar fechas con notas", e);
        }
    }

    public void editarNota(int usuarioId, LocalDate fecha, String textoActual, String textoNuevo)
            throws ColiseumException {
        Validador.fecha(fecha);
        String nuevo = Validador.texto("Nota", textoNuevo, MAX_NOTA);
        try (Connection con = Conexion.obtener()) {
            if (!diaDao.actualizarPorTexto(con, usuarioId, fecha, textoActual, nuevo)) {
                throw new ValidacionException("La nota ya no existe.");
            }
        } catch (SQLException e) {
            throw AccesoDatosException.de("Editar nota", e);
        }
    }

    public void eliminarNota(int usuarioId, LocalDate fecha, String texto) throws ColiseumException {
        try (Connection con = Conexion.obtener()) {
            diaDao.eliminarPorTexto(con, usuarioId, fecha, texto);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Eliminar nota", e);
        }
    }
}
