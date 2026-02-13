package proyecto.bitcoin;

import java.nio.charset.StandardCharsets;

// Helpers de bytes y conversiones para el prototipo
public class BytesUtil {

    static byte[] encodeInt(int n) {
        // Prototipo simple: 0..16 se guarda en 1 byte
        return new byte[]{ (byte) n };
    }

    static int decodeInt(byte[] b) {
        if (b == null || b.length == 0) return 0;
        return b[0] & 0xFF;
    }

    static boolean castToBool(byte[] b) {
        if (b == null) return false;
        for (byte x : b) if (x != 0) return true;
        return false;
    }

    static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    static String asString(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    // Convierte HEX a bytes (para poder empujar pubKeyHash en PUSHDATA)
    static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("El hex es invalido: " + hex);
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }

    static String toHex(byte[] b) {
        char[] hex = "0123456789abcdef".toCharArray();
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(hex[(x >> 4) & 0xF]);
            sb.append(hex[x & 0xF]);
        }
        return sb.toString();
    }
}
