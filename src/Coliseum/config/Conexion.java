package config;

import excepciones.AccesoDatosException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones a PostgreSQL (local en Docker o Aiven en la nube) con los datos de las variables de entorno:
 * DB_HOST, DB_PORT (5432), DB_NAME, DB_USER, DB_PASSWORD, DB_SSL_MODE (REQUIRED por defecto)
 * y DB_SSL_ROOT_CERT (opcional: ruta al ca.pem de Aiven para verificar el servidor).
 *
 * DB_SSL_MODE acepta:  DISABLED (sin SSL, solo base local en Docker),
 *                      REQUIRED (cifrado obligatorio),
 *                      VERIFY_CA / VERIFY_IDENTITY (cifrado + verifica el certificado con DB_SSL_ROOT_CERT).
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
        String puerto = Configuracion.obtener("DB_PORT", "5432");
        String base = exigir("DB_NAME");
        String usuario = exigir("DB_USER");
        String clave = exigir("DB_PASSWORD");
        String ssl = modoSslPostgres(Configuracion.obtener("DB_SSL_MODE", "REQUIRED"));
        String certificado = Configuracion.obtener("DB_SSL_ROOT_CERT");
        if (ssl.startsWith("verify") && certificado == null) {
            throw new AccesoDatosException("Con DB_SSL_MODE=" + Configuracion.obtener("DB_SSL_MODE")
                    + " hay que indicar DB_SSL_ROOT_CERT (ruta al ca.pem de Aiven).");
        }

        String url = "jdbc:postgresql://" + host + ':' + puerto + '/' + base;

        // Credenciales y opciones van en Properties, no en la URL (así no aparecen en mensajes ni logs).
        Properties props = new Properties();
        props.setProperty("user", usuario);
        props.setProperty("password", clave);
        props.setProperty("sslmode", ssl);
        if (certificado != null) {
            props.setProperty("sslrootcert", certificado);
        }
        props.setProperty("connectTimeout", "8");
        props.setProperty("ApplicationName", "Coliseum");
        try {
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            throw AccesoDatosException.de("Conectar a " + descripcion(), e);
        }
    }

    /**
     * Traduce DB_SSL_MODE al nombre que usa el driver de PostgreSQL.
     * Acepta los nombres estilo MySQL (REQUIRED, DISABLED...) y también los de PostgreSQL (require, disable...).
     */
    static String modoSslPostgres(String valor) throws AccesoDatosException {
        switch (valor.trim().toUpperCase().replace('-', '_')) {
            case "DISABLED":
            case "DISABLE":
                return "disable";
            case "PREFERRED":
            case "PREFER":
                return "prefer";
            case "REQUIRED":
            case "REQUIRE":
                return "require";
            case "VERIFY_CA":
                return "verify-ca";
            case "VERIFY_IDENTITY":
            case "VERIFY_FULL":
                return "verify-full";
            default:
                throw new AccesoDatosException("DB_SSL_MODE inválido: usá DISABLED, REQUIRED, VERIFY_CA o VERIFY_IDENTITY.");
        }
    }

    /** host:puerto/base, sin credenciales. Sirve para mostrar y registrar. */
    public static String descripcion() {
        return Configuracion.obtener("DB_HOST", "?") + ":" + Configuracion.obtener("DB_PORT", "5432")
                + "/" + Configuracion.obtener("DB_NAME", "?");
    }
}
