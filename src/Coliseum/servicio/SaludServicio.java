package servicio;

import config.Conexion;
import config.Registro;
import excepciones.ColiseumException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.EstadoServicio;

/** R12: verifica que la aplicación pueda hablar con la base de datos (equivale al /health de un proyecto web). */
public class SaludServicio {

    public EstadoServicio verificar() {
        String destino = Conexion.descripcion();
        long inicio = System.nanoTime();
        try (Connection con = Conexion.obtener()) {
            try (PreparedStatement st = con.prepareStatement("SELECT 1"); ResultSet rs = st.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    return new EstadoServicio(false, "La base respondió algo inesperado.", 0, destino, false);
                }
            }
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            return new EstadoServicio(true, "La aplicación y la base de datos funcionan.", ms, destino, usaSsl(con));
        } catch (ColiseumException e) {
            return new EstadoServicio(false, e.getMessage(), 0, destino, false);
        } catch (SQLException e) {
            Registro.error("Verificar estado", e);
            return new EstadoServicio(false, "No se pudo consultar la base de datos.", 0, destino, false);
        }
    }

    /** Pregunta a PostgreSQL si la conexión actual está cifrada (vista pg_stat_ssl). */
    private boolean usaSsl(Connection con) {
        try (PreparedStatement st = con.prepareStatement("SELECT ssl FROM pg_stat_ssl WHERE pid = pg_backend_pid()");
             ResultSet rs = st.executeQuery()) {
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            return false;
        }
    }
}
