package proyecto.bitcoin;

import java.security.MessageDigest;
import java.util.Arrays;

// Cripto simulada para el prototipo 
public class CryptoMock {
    // OP_HASH160 real = RIPEMD160(SHA256(x)), pero aqui lo simulamos:
    // SHA-256(x) y recortamos a 20 bytes (como longitud tipica hash160).
    static byte[] hash160Mock(byte[] data) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] h = sha256.digest(data);
            return Arrays.copyOf(h, 20);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo usar hash160Mock: " + e.getMessage(), e);
        }
    }

    // Firma simulada: consideramos "valida" si sig == "SIG(" + pubKey + ")"
    static boolean checkSigMock(byte[] sig, byte[] pubKey) {
        String expected = "SIG(" + BytesUtil.asString(pubKey) + ")";
        return expected.equals(BytesUtil.asString(sig));

    }
}
