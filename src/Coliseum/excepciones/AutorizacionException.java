package excepciones;

/** El usuario no tiene el rol necesario para realizar la operación. */
public class AutorizacionException extends ColiseumException {
    private static final long serialVersionUID = 1L;

    public AutorizacionException(String mensaje) {
        super(mensaje);
    }
}
