package excepciones;

import config.Registro;
import java.sql.SQLException;

/**
 * Falló el acceso a la base de datos. El mensaje es genérico (apto para el usuario);
 * el detalle técnico se guarda en el registro (coliseum.log) y no se muestra en pantalla.
 */
public class AccesoDatosException extends ColiseumException {
    private static final long serialVersionUID = 1L;

    public AccesoDatosException(String mensaje) {
        super(mensaje);
    }

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Registra el detalle técnico y devuelve una excepción con un mensaje seguro para el usuario. */
    public static AccesoDatosException de(String contexto, SQLException e) {
        Registro.error(contexto, e);
        return new AccesoDatosException("No se pudo completar la operación con la base de datos. "
                + "Verificá la conexión e intentá de nuevo.", e);
    }
}
