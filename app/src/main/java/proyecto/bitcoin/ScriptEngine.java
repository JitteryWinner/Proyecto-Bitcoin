package proyecto.bitcoin;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

// Evaluador de Script: maquina de pila con opcodes minimos
public class ScriptEngine {

    static void require(boolean cond, String msg) {
        if (!cond) throw new RuntimeException("Error de Script: " + msg);
    }

    static byte[] pop(Deque<byte[]> st) {
        require(!st.isEmpty(), "la pila esta vacia");
        return st.removeLast();
    }

    static void push(Deque<byte[]> st, byte[] v) {
        st.addLast(v);
    }

    // Ejecuta el programa (tokens separados por espacios)
    // Retorna true si la pila final termina con TRUE (1)
    public static boolean eval(String program, boolean trace) {
        Deque<byte[]> stack = new ArrayDeque<>();

        String[] t = program.trim().isEmpty() ? new String[0] : program.trim().split("\\s+");
        for (int i = 0; i < t.length; i++) {
            String tok = t[i];

            // Literales numericos 1..16 
            if (tok.matches("\\d+")) {
                int n = Integer.parseInt(tok);
                push(stack, BytesUtil.encodeInt(n));
                if (trace) dump("PUSH_NUM(" + n + ")", stack);
                continue;
            }

            // PUSHDATA <dato>  (si <dato> es hex, lo decodifica a bytes)
            if (tok.equalsIgnoreCase("PUSHDATA")) {
                require(i + 1 < t.length, "PUSHDATA sin dato");
                String data = t[++i];

                // si parece hex (solo 0-9a-f y longitud par), lo convertimos a bytes
                if (data.matches("(?i)[0-9a-f]+") && (data.length() % 2 == 0)) {
                    push(stack, BytesUtil.fromHex(data));
                } else {
                    push(stack, BytesUtil.bytes(data));
                }

                if (trace) dump("PUSHDATA(" + data + ")", stack);
                continue;
            }

            // Si viene como <algo> lo tratamos como dato directo (estilo script)
            if (tok.startsWith("<") && tok.endsWith(">") && tok.length() >= 2) {
                String data = tok.substring(1, tok.length() - 1);
                push(stack, BytesUtil.bytes(data));
                if (trace) dump("PUSH_DATA(<" + data + ">)", stack);
                continue;
            }

            // Opcodes minimos
            switch (tok) {
                case "OP_0":
                case "OP_FALSE":
                    push(stack, BytesUtil.encodeInt(0));
                    if (trace) dump(tok, stack);
                    break;

                default:
                    // OP_1..OP_16
                    if (tok.startsWith("OP_")) {
                        String rest = tok.substring(3);
                        if (rest.matches("\\d+")) {
                            int n = Integer.parseInt(rest);
                            require(n >= 1 && n <= 16, "OP_" + n + " fuera del rango 1..16");
                            push(stack, BytesUtil.encodeInt(n));
                            if (trace) dump(tok, stack);
                            break;
                        }
                    }

                    if (tok.equals("OP_DUP")) {
                        byte[] a = pop(stack);
                        push(stack, a);
                        push(stack, a); // duplicamos referencia (suficiente para prototipo)
                        if (trace) dump(tok, stack);

                    } else if (tok.equals("OP_DROP")) {
                        pop(stack);
                        if (trace) dump(tok, stack);

                    } else if (tok.equals("OP_EQUAL")) {
                        byte[] b = pop(stack);
                        byte[] a = pop(stack);
                        boolean eq = Arrays.equals(a, b);
                        push(stack, BytesUtil.encodeInt(eq ? 1 : 0));
                        if (trace) dump(tok, stack);

                    } else if (tok.equals("OP_EQUALVERIFY")) {
                        byte[] b = pop(stack);
                        byte[] a = pop(stack);
                        require(Arrays.equals(a, b), "OP_EQUALVERIFY fallo");
                        if (trace) dump(tok, stack);

                    } else if (tok.equals("OP_HASH160")) {
                        byte[] a = pop(stack);
                        push(stack, CryptoMock.hash160Mock(a));
                        if (trace) dump(tok, stack);

                    } else if (tok.equals("OP_CHECKSIG")) {
                        // Orden: ... <sig> <pubKey> OP_CHECKSIG
                        byte[] pubKey = pop(stack);
                        byte[] sig = pop(stack);
                        boolean ok = CryptoMock.checkSigMock(sig, pubKey);
                        push(stack, BytesUtil.encodeInt(ok ? 1 : 0));
                        if (trace) dump(tok, stack);

                    } else {
                        throw new RuntimeException("Opcode/token no soportado en prototipo: " + tok);
                    }
                    break;
            }
        }

        // exito si el top stack es "true" y no fallo nada
        if (trace) dump("END", stack);
        require(!stack.isEmpty(), "pila final vacia");
        return BytesUtil.castToBool(stack.peekLast());
    }

    // Muestra el stack en cada paso (simple)
    static void dump(String step, Deque<byte[]> st) {
        System.out.print(step + " | stack=[");
        boolean first = true;
        for (byte[] v : st) {
            if (!first) System.out.print(", ");
            first = false;

            // Si es 1 byte, mostramos como int; si no, mostramos como texto
            if (v.length == 1) {
                System.out.print(BytesUtil.decodeInt(v));
            } else {
                String s = BytesUtil.asString(v);
                System.out.print("\"" + s + "\"");
            }
        }
        System.out.println("]");
    }
}
