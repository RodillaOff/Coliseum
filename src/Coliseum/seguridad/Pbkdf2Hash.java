package seguridad;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash actual: PBKDF2-HMAC-SHA256 con sal aleatoria y 120.000 iteraciones (viene con el JDK, sin librerías).
 * Formato guardado: pbkdf2$iteraciones$sal(base64)$hash(base64).
 * Es una alternativa a BCrypt: lento a propósito y con sal propia por contraseña.
 */
public class Pbkdf2Hash implements HashContrasena {

    private static final String PREFIJO = "pbkdf2$";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Override
    public String hashear(String plano) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(plano, sal, ITERACIONES);
        return PREFIJO + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean verifica(String plano, String guardado) {
        if (!reconoce(guardado)) {
            return false;
        }
        String[] partes = guardado.split("\\$");
        if (partes.length != 4) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(plano, sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado); // comparación en tiempo constante
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean reconoce(String guardado) {
        return guardado != null && guardado.startsWith(PREFIJO);
    }

    private static byte[] derivar(String plano, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plano.toCharArray(), sal, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible en este JDK", e);
        }
    }
}
