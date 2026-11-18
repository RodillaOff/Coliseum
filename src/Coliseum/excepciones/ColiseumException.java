package excepciones;

/** Excepción base del dominio. Su mensaje siempre es apto para mostrarle al usuario. */
public class ColiseumException extends Exception {
    private static final long serialVersionUID = 1L;

    public ColiseumException(String mensaje) {
        super(mensaje);
    }

    public ColiseumException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
