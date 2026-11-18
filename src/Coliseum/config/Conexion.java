package config;

import excepciones.AccesoDatosException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones a MySQL (local en Docker o Aiven en la nube) con los datos de las variables de entorno:
 * DB_HOST, DB_PORT (3306), DB_NAME, DB_USER, DB_PASSWORD y DB_SSL_MODE (REQUIRED por defecto).
 */
public final class Conexion {

    private Conexion() {
    }

    private static String exigir(String clave) throws AccesoDatosException {
        String valor = Configuracion.obtener(clave);
        if (valor == null) {
            throw new AccesoDatosException("Falta la variable de entorno " + clave
                    + ". Copiá .env.example a .env y completalo (ver README).");
        }
        return valor;
    }

    /** Abre una nueva conexión. Quien la llama debe cerrarla. */
    public static Connection obtener() throws AccesoDatosException {
        String host = exigir("DB_HOST");
        String puerto = Configuracion.obtener("DB_PORT", "3306");
        String base = exigir("DB_NAME");
        String usuario = exigir("DB_USER");
        String clave = exigir("DB_PASSWORD");
        String ssl = Configuracion.obtener("DB_SSL_MODE", "REQUIRED").toUpperCase();

        StringBuilder url = new StringBuilder("jdbc:mysql://").append(host).append(':').append(puerto)
                .append('/').append(base)
                .append("?sslMode=").append(ssl)
                .append("&serverTimezone=UTC&connectTimeout=8000");
        if ("DISABLED".equals(ssl)) {
            // Solo para la base local en Docker (sin SSL): MySQL 8 pide recuperar la clave pública.
            url.append("&allowPublicKeyRetrieval=true");
        }

        Properties props = new Properties();
        props.setProperty("user", usuario);
        props.setProperty("password", clave);
        try {
            return DriverManager.getConnection(url.toString(), props);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Conectar a " + descripcion(), e);
        }
    }

    /** host:puerto/base, sin credenciales. Sirve para mostrar y registrar. */
    public static String descripcion() {
        return Configuracion.obtener("DB_HOST", "?") + ":" + Configuracion.obtener("DB_PORT", "3306")
                + "/" + Configuracion.obtener("DB_NAME", "?");
    }
}
