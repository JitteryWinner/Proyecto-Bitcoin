package proyecto.bitcoin;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Utilidades para conversión entre enteros, cadenas, bytes y hexadecimal.
 */
public final class BytesUtil {

    private BytesUtil() {
        // Evita instanciación.
    }

    /**
     * Codifica un entero en 4 bytes.
     *
     * @param value entero a codificar
     * @return arreglo de bytes con el valor codificado
     */
    public static byte[] encodeInt(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * Decodifica un entero desde 4 bytes.
     *
     * @param bytes bytes a decodificar
     * @return entero resultante
     */
    public static int decodeInt(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            throw new IllegalArgumentException("Se esperaban exactamente 4 bytes para un entero.");
        }
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * Convierte una cadena a bytes UTF-8.
     *
     * @param value cadena de entrada
     * @return bytes UTF-8
     */
    public static byte[] toBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Convierte bytes UTF-8 a cadena.
     *
     * @param bytes arreglo de bytes
     * @return cadena convertida
     */
    public static String toUtf8String(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Convierte un valor binario a booleano siguiendo una convención simple:
     * false si todos los bytes son 0; true en cualquier otro caso.
     *
     * @param bytes bytes a evaluar
     * @return true si representa verdadero
     */
    public static boolean castToBool(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        for (byte currentByte : bytes) {
            if (currentByte != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convierte un arreglo de bytes a representación hexadecimal.
     *
     * @param bytes bytes de entrada
     * @return cadena hexadecimal en minúsculas
     */
    public static String toHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);

        for (byte currentByte : bytes) {
            stringBuilder.append(String.format("%02x", currentByte));
        }

        return stringBuilder.toString();
    }

    /**
     * Convierte una cadena hexadecimal a bytes.
     *
     * @param hex cadena hexadecimal
     * @return arreglo de bytes
     */
    public static byte[] fromHex(String hex) {
        if (hex == null || hex.isBlank()) {
            throw new IllegalArgumentException("La cadena hexadecimal no puede estar vacía.");
        }

        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("La cadena hexadecimal debe tener longitud par.");
        }

        byte[] result = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length(); i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);

            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Hexadecimal inválido: " + hex);
            }

            result[i / 2] = (byte) ((high << 4) + low);
        }

        return result;
    }

    /**
     * Intenta mostrar un byte[] como entero; si no es posible, lo muestra
     * como texto UTF-8 o hexadecimal.
     *
     * @param bytes bytes a representar
     * @return representación legible
     */
    public static String toReadableString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }

        if (bytes.length == 4) {
            try {
                return Integer.toString(decodeInt(bytes));
            } catch (IllegalArgumentException ignored) {
                // Continúa a otras representaciones.
            }
        }

        String utf8 = toUtf8String(bytes);
        boolean printable = utf8.chars().allMatch(ch -> ch >= 32 && ch <= 126);

        if (printable) {
            return "\"" + utf8 + "\"";
        }

        return "0x" + toHex(bytes);
    }
}