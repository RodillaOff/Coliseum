package config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * R10: lee la configuración desde variables de entorno.
 * Para comodidad en la PC (donde es incómodo definir variables), si una variable no está definida
 * se busca en un archivo .env (en la carpeta actual o hasta 3 carpetas más arriba).
 * Nunca hay valores por defecto para usuario ni contraseña.
 */
public final class Configuracion {

    private static Map<String, String> archivoEnv;

    private Configuracion() {
    }

    /** Devuelve el valor de la variable o null si no está definida. */
    public static synchronized String obtener(String clave) {
        String valor = System.getenv(clave);
        if (valor != null && !valor.trim().isEmpty()) {
            return valor.trim();
        }
        return leerArchivoEnv().get(clave);
    }

    public static String obtener(String clave, String porDefecto) {
        String valor = obtener(clave);
        return valor == null ? porDefecto : valor;
    }

    private static Map<String, String> leerArchivoEnv() {
        if (archivoEnv != null) {
            return archivoEnv;
        }
        archivoEnv = new HashMap<>();
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 4 && dir != null; i++, dir = dir.getParent()) {
            Path archivo = dir.resolve(".env");
            if (Files.isRegularFile(archivo)) {
                try {
                    List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
                    for (String linea : lineas) {
                        String l = linea.trim();
                        int igual = l.indexOf('=');
                        if (l.isEmpty() || l.startsWith("#") || igual <= 0) {
                            continue;
                        }
                        String valor = l.substring(igual + 1).trim();
                        if (valor.length() >= 2 && (valor.startsWith("\"") && valor.endsWith("\"")
                                || valor.startsWith("'") && valor.endsWith("'"))) {
                            valor = valor.substring(1, valor.length() - 1);
                        }
                        if (!valor.isEmpty()) {
                            archivoEnv.put(l.substring(0, igual).trim(), valor);
                        }
                    }
                } catch (IOException e) {
                    Registro.error("Leer .env", e);
                }
                break;
            }
        }
        return archivoEnv;
    }
}
