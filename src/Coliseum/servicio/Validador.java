package servicio;

import excepciones.ValidacionException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** R8: validaciones de todas las entradas. Los mensajes están pensados para mostrarse al usuario. */
public final class Validador {

    private static final Pattern NOMBRE = Pattern.compile("[\\p{L}\\p{N}_.-]+");
    private static final Pattern HORA = Pattern.compile("^([01]?\\d|2[0-3]):([0-5]\\d)$");

    private Validador() {
    }

    public static String nombreUsuario(String nombre) throws ValidacionException {
        String n = nombre == null ? "" : nombre.trim();
        if (n.length() < 3 || n.length() > 30) {
            throw new ValidacionException("El usuario debe tener entre 3 y 30 caracteres.");
        }
        if (!NOMBRE.matcher(n).matches()) {
            throw new ValidacionException("El usuario solo puede tener letras, números, punto, guion y guion bajo.");
        }
        return n;
    }

    public static void contrasena(String contrasena) throws ValidacionException {
        if (contrasena == null || contrasena.length() < 6) {
            throw new ValidacionException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (contrasena.length() > 72) {
            throw new ValidacionException("La contraseña no puede superar los 72 caracteres.");
        }
    }

    /** Texto obligatorio con largo máximo. Devuelve el texto sin espacios en los extremos. */
    public static String texto(String campo, String valor, int maximo) throws ValidacionException {
        String t = valor == null ? "" : valor.trim();
        if (t.isEmpty()) {
            throw new ValidacionException("El campo \"" + campo + "\" no puede estar vacío.");
        }
        if (t.length() > maximo) {
            throw new ValidacionException("El campo \"" + campo + "\" no puede superar los " + maximo + " caracteres.");
        }
        return t;
    }

    /** Texto opcional (puede ser vacío) con largo máximo. */
    public static String textoOpcional(String campo, String valor, int maximo) throws ValidacionException {
        String t = valor == null ? "" : valor.trim();
        if (t.length() > maximo) {
            throw new ValidacionException("El campo \"" + campo + "\" no puede superar los " + maximo + " caracteres.");
        }
        return t;
    }

    /** Devuelve "" si no hay hora, "HH:mm" si es válida; si no, lanza ValidacionException. */
    public static String hora(String texto) throws ValidacionException {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        Matcher m = HORA.matcher(texto.trim());
        if (!m.matches()) {
            throw new ValidacionException("La hora debe tener el formato HH:mm (por ejemplo 18:30).");
        }
        return String.format("%02d:%02d", Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    }

    public static LocalDate fecha(LocalDate fecha) throws ValidacionException {
        if (fecha == null) {
            throw new ValidacionException("Falta la fecha.");
        }
        if (fecha.getYear() < 2000 || fecha.getYear() > 2100) {
            throw new ValidacionException("La fecha está fuera del rango permitido (2000 a 2100).");
        }
        return fecha;
    }

    public static void rango(LocalDate desde, LocalDate hasta) throws ValidacionException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("La fecha \"desde\" no puede ser posterior a \"hasta\".");
        }
    }
}
