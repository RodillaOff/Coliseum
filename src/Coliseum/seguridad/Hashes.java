package seguridad;

/** Elige el algoritmo según el formato del hash guardado (uso polimórfico de HashContrasena). */
public final class Hashes {

    private static final HashContrasena ACTUAL = new Pbkdf2Hash();
    private static final HashContrasena[] CONOCIDOS = {ACTUAL, new Sha256Hash()};

    private Hashes() {
    }

    public static HashContrasena actual() {
        return ACTUAL;
    }

    public static boolean verificar(String plano, String guardado) {
        for (HashContrasena h : CONOCIDOS) {
            if (h.reconoce(guardado)) {
                return h.verifica(plano, guardado);
            }
        }
        return false;
    }

    /** true si el hash guardado no es del algoritmo actual y conviene regenerarlo. */
    public static boolean necesitaActualizar(String guardado) {
        return !ACTUAL.reconoce(guardado);
    }
}
