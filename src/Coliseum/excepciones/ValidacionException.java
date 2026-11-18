package excepciones;

/** Un dato ingresado no cumple las reglas (vacío, demasiado largo, formato inválido, etc.). */
public class ValidacionException extends ColiseumException {
    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
