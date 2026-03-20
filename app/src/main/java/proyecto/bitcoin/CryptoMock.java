package proyecto.bitcoin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Implementación simulada de operaciones criptográficas para fines didácticos.
 */
public final class CryptoMock {

    private CryptoMock() {
        // Evita instanciación.
    }

    /**
     * Simula OP_SHA256.
     *
     * @param data datos de entrada
     * @return hash SHA-256
     */
    public static byte[] sha256(byte[] data) {
        return digest("SHA-256", data);
    }

    /**
     * Simula OP_HASH256 = SHA256(SHA256(data)).
     *
     * @param data datos de entrada
     * @return hash doble SHA-256
     */
    public static byte[] hash256(byte[] data) {
        return sha256(sha256(data));
    }

    /**
     * Simula OP_HASH160. En Bitcoin real sería RIPEMD160(SHA256(x)).
     * Aquí se aproxima usando SHA-256 y recortando a 20 bytes.
     *
     * @param data datos de entrada
     * @return hash simulado de 20 bytes
     */
    public static byte[] hash160(byte[] data) {
        byte[] sha256Result = sha256(data);
        return Arrays.copyOf(sha256Result, 20);
    }

    /**
     * Verificación simulada de firma.
     * Se considera válida si sig = "SIG(" + pubKey + ")".
     *
     * @param signature firma
     * @param publicKey llave pública
     * @return true si la firma es válida en esta simulación
     */
    public static boolean checkSig(byte[] signature, byte[] publicKey) {
        String expectedSignature = "SIG(" + BytesUtil.toUtf8String(publicKey) + ")";
        String receivedSignature = BytesUtil.toUtf8String(signature);
        return expectedSignature.equals(receivedSignature);
    }

    /**
     * Ejecuta un digest con el algoritmo indicado.
     *
     * @param algorithm nombre del algoritmo
     * @param data datos de entrada
     * @return hash resultante
     */
    private static byte[] digest(String algorithm, byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            return messageDigest.digest(data);
        } catch (NoSuchAlgorithmException exception) {
            throw new ScriptException("No se pudo ejecutar el algoritmo criptográfico: " + algorithm, exception);
        }
    }
}