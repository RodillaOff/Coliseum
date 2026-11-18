package config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/** Registro de errores técnicos. Escribe en coliseum.log; nunca en pantalla ni con contraseñas. */
public final class Registro {

    private Registro() {
    }

    public static void error(String contexto, Throwable t) {
        String linea = LocalDateTime.now() + " [ERROR] " + contexto + ": "
                + t.getClass().getSimpleName() + " - " + t.getMessage() + System.lineSeparator();
        try {
            Files.write(Paths.get("coliseum.log"), linea.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignorada) {
            System.err.print(linea); // último recurso: consola
        }
    }
}
