package servicio;

import java.sql.Connection;
import java.sql.SQLException;

/** Utilidades para manejar transacciones sin repetir código. */
final class Conexiones {

    private Conexiones() {
    }

    static void revertir(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ignorada) {
            // si el rollback falla, la conexión se cierra igual y la base descarta la transacción
        }
    }

    static void cerrar(Connection con) {
        try {
            con.setAutoCommit(true);
        } catch (SQLException ignorada) {
            // se cierra igual
        }
        try {
            con.close();
        } catch (SQLException ignorada) {
            // nada más que hacer
        }
    }
}
