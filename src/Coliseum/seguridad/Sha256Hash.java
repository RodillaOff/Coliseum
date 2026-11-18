package seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash heredado de la primera versión del proyecto (SHA-256 sin sal, 64 caracteres hexadecimales).
 * Solo se usa para verificar cuentas viejas: al iniciar sesión, la contraseña se vuelve a guardar con Pbkdf2Hash.
 */
public class Sha256Hash implements HashContrasena {

    @Override
    public String hashear(String plano) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(plano.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean verifica(String plano, String guardado) {
        return reconoce(guardado) && MessageDigest.isEqual(
                hashear(plano).getBytes(StandardCharsets.UTF_8), guardado.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean reconoce(String guardado) {
        return guardado != null && guardado.matches("[0-9a-f]{64}");
    }
}
