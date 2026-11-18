package seguridad;

/** Interfaz para hashear y verificar contraseñas. Tiene dos implementaciones (R5): Pbkdf2Hash y Sha256Hash. */
public interface HashContrasena {

    /** Devuelve el hash a guardar en la base. */
    String hashear(String plano);

    /** Compara una contraseña en texto plano con un hash guardado. */
    boolean verifica(String plano, String guardado);

    /** Indica si este algoritmo es el que generó el hash guardado. */
    boolean reconoce(String guardado);
}
