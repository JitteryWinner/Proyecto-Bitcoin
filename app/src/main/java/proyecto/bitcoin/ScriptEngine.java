package proyecto.bitcoin;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Intérprete de un subconjunto de Bitcoin Script.
 *
 * El evaluador procesa tokens de izquierda a derecha usando una pila principal.
 * El programa es válido si termina sin errores y la cima final de la pila es verdadera.</p>
 */
public class ScriptEngine {

    private final boolean traceEnabled;

    /**
     * Crea un nuevo intérprete.
     *
     * @param traceEnabled true para imprimir el estado de la pila en cada paso
     */
    public ScriptEngine(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    /**
     * Evalúa un programa Bitcoin Script.
     *
     * @param program programa separado por espacios
     * @return true si la ejecución finaliza correctamente y la cima de la pila es verdadera
     */
    public boolean eval(String program) {
        Deque<byte[]> stack = new ArrayDeque<>();
        Deque<Boolean> executionStack = new ArrayDeque<>();

        String[] tokens = tokenize(program);

        for (int index = 0; index < tokens.length; index++) {
            String token = tokens[index];

            if (isControlFlowToken(token)) {
                index = processControlFlowToken(tokens, index, token, stack, executionStack);
                continue;
            }

            if (!shouldExecute(executionStack)) {
                continue;
            }

            if (isNumericLiteral(token)) {
                push(stack, BytesUtil.encodeInt(Integer.parseInt(token)));
                trace(token, stack);
                continue;
            }

            if ("PUSHDATA".equalsIgnoreCase(token)) {
                validate(index + 1 < tokens.length, "PUSHDATA sin dato asociado.");
                String dataToken = tokens[++index];
                push(stack, parseDataToken(dataToken));
                trace("PUSHDATA " + dataToken, stack);
                continue;
            }

            if (isBracketedData(token)) {
                push(stack, parseDataToken(token));
                trace(token, stack);
                continue;
            }

            executeOpcode(token, stack);
            trace(token, stack);
        }

        validate(executionStack.isEmpty(), "Estructura condicional incompleta: falta OP_ENDIF.");
        validate(!stack.isEmpty(), "La pila final quedó vacía.");

        return BytesUtil.castToBool(stack.peekLast());
    }

    /**
     * Tokeniza el programa a partir de espacios.
     *
     * @param program programa fuente
     * @return tokens del script
     */
    private String[] tokenize(String program) {
        if (program == null || program.isBlank()) {
            return new String[0];
        }
        return program.trim().split("\\s+");
    }

    /**
     * Ejecuta un opcode no relacionado con control de flujo.
     *
     * @param opcode opcode a ejecutar
     * @param stack pila principal
     */
    private void executeOpcode(String opcode, Deque<byte[]> stack) {
        switch (opcode) {
            case "OP_0":
            case "OP_FALSE":
                push(stack, BytesUtil.encodeInt(0));
                break;

            case "OP_1":
            case "OP_2":
            case "OP_3":
            case "OP_4":
            case "OP_5":
            case "OP_6":
            case "OP_7":
            case "OP_8":
            case "OP_9":
            case "OP_10":
            case "OP_11":
            case "OP_12":
            case "OP_13":
            case "OP_14":
            case "OP_15":
            case "OP_16":
                push(stack, BytesUtil.encodeInt(Integer.parseInt(opcode.substring(3))));
                break;

            case "OP_DUP":
                executeDup(stack);
                break;

            case "OP_DROP":
                pop(stack);
                break;

            case "OP_SWAP":
                executeSwap(stack);
                break;

            case "OP_OVER":
                executeOver(stack);
                break;

            case "OP_EQUAL":
                executeEqual(stack);
                break;

            case "OP_EQUALVERIFY":
                executeEqualVerify(stack);
                break;

            case "OP_NOT":
                executeNot(stack);
                break;

            case "OP_BOOLAND":
                executeBoolAnd(stack);
                break;

            case "OP_BOOLOR":
                executeBoolOr(stack);
                break;

            case "OP_ADD":
                executeAdd(stack);
                break;

            case "OP_SUB":
                executeSub(stack);
                break;

            case "OP_NUMEQUALVERIFY":
                executeNumEqualVerify(stack);
                break;

            case "OP_LESSTHAN":
                executeLessThan(stack);
                break;

            case "OP_GREATERTHAN":
                executeGreaterThan(stack);
                break;

            case "OP_LESSTHANOREQUAL":
                executeLessThanOrEqual(stack);
                break;

            case "OP_GREATERTHANOREQUAL":
                executeGreaterThanOrEqual(stack);
                break;

            case "OP_VERIFY":
                executeVerify(stack);
                break;

            case "OP_RETURN":
                throw new ScriptException("La ejecución falló con OP_RETURN");

            case "OP_SHA256":
                executeSha256(stack);
                break;

            case "OP_HASH160":
                executeHash160(stack);
                break;

            case "OP_HASH256":
                executeHash256(stack);
                break;

            case "OP_CHECKSIG":
                executeCheckSig(stack);
                break;

            case "OP_CHECKSIGVERIFY":
                executeCheckSigVerify(stack);
                break;

            default:
                throw new ScriptException("Opcode no soportado: " + opcode);
        }
    }

    private void executeDup(Deque<byte[]> stack) {
        byte[] topElement = pop(stack);
        push(stack, topElement);
        push(stack, Arrays.copyOf(topElement, topElement.length));
    }

    private void executeSwap(Deque<byte[]> stack) {
        validate(stack.size() >= 2, "OP_SWAP requiere al menos 2 elementos.");
        byte[] first = pop(stack);
        byte[] second = pop(stack);
        push(stack, first);
        push(stack, second);
    }

    private void executeOver(Deque<byte[]> stack) {
        validate(stack.size() >= 2, "OP_OVER requiere al menos 2 elementos.");
        byte[] top = pop(stack);
        byte[] second = pop(stack);

        push(stack, second);
        push(stack, top);
        push(stack, Arrays.copyOf(second, second.length));
    }

    private void executeEqual(Deque<byte[]> stack) {
        byte[] first = pop(stack);
        byte[] second = pop(stack);
        push(stack, BytesUtil.encodeInt(Arrays.equals(first, second) ? 1 : 0));
    }

    private void executeEqualVerify(Deque<byte[]> stack) {
        byte[] first = pop(stack);
        byte[] second = pop(stack);
        validate(Arrays.equals(first, second), "OP_EQUALVERIFY falló.");
    }

    private void executeNot(Deque<byte[]> stack) {
        byte[] value = pop(stack);
        boolean boolValue = BytesUtil.castToBool(value);
        push(stack, BytesUtil.encodeInt(boolValue ? 0 : 1));
    }

    private void executeBoolAnd(Deque<byte[]> stack) {
        boolean first = BytesUtil.castToBool(pop(stack));
        boolean second = BytesUtil.castToBool(pop(stack));
        push(stack, BytesUtil.encodeInt((first && second) ? 1 : 0));
    }

    private void executeBoolOr(Deque<byte[]> stack) {
        boolean first = BytesUtil.castToBool(pop(stack));
        boolean second = BytesUtil.castToBool(pop(stack));
        push(stack, BytesUtil.encodeInt((first || second) ? 1 : 0));
    }

    private void executeAdd(Deque<byte[]> stack) {
        int first = popInt(stack);
        int second = popInt(stack);
        push(stack, BytesUtil.encodeInt(second + first));
    }

    private void executeSub(Deque<byte[]> stack) {
        int first = popInt(stack);
        int second = popInt(stack);
        push(stack, BytesUtil.encodeInt(second - first));
    }

    private void executeNumEqualVerify(Deque<byte[]> stack) {
        int first = popInt(stack);
        int second = popInt(stack);
        validate(first == second, "OP_NUMEQUALVERIFY falló.");
    }

    private void executeLessThan(Deque<byte[]> stack) {
        int first = popInt(stack);
        int second = popInt(stack);
        push(stack, BytesUtil.encodeInt(second < first ? 1 : 0));
    }

    private void executeGreaterThan(Deque<byte[]> stack) {
        int first = popInt(stack);
        int second = popInt(stack);
        push(stack, BytesUtil.encodeInt(second > first ? 1 : 0));
    }

    private void executeLessThanOrEqual(Deque<byte[]> stack) {
        int first = popInt(stack);
        int second = popInt(stack);
        push(stack, BytesUtil.encodeInt(second <= first ? 1 : 0));
    }

    private void executeGreaterThanOrEqual(Deque<byte[]> stack) {
        int first = popInt(stack);
        int second = popInt(stack);
        push(stack, BytesUtil.encodeInt(second >= first ? 1 : 0));
    }

    private void executeVerify(Deque<byte[]> stack) {
        boolean result = BytesUtil.castToBool(pop(stack));
        validate(result, "OP_VERIFY falló.");
    }

    private void executeSha256(Deque<byte[]> stack) {
        byte[] value = pop(stack);
        push(stack, CryptoMock.sha256(value));
    }

    private void executeHash160(Deque<byte[]> stack) {
        byte[] value = pop(stack);
        push(stack, CryptoMock.hash160(value));
    }

    private void executeHash256(Deque<byte[]> stack) {
        byte[] value = pop(stack);
        push(stack, CryptoMock.hash256(value));
    }

    private void executeCheckSig(Deque<byte[]> stack) {
        byte[] publicKey = pop(stack);
        byte[] signature = pop(stack);
        boolean valid = CryptoMock.checkSig(signature, publicKey);
        push(stack, BytesUtil.encodeInt(valid ? 1 : 0));
    }

    private void executeCheckSigVerify(Deque<byte[]> stack) {
        byte[] publicKey = pop(stack);
        byte[] signature = pop(stack);
        boolean valid = CryptoMock.checkSig(signature, publicKey);
        validate(valid, "OP_CHECKSIGVERIFY falló.");
    }

    /**
     * Procesa tokens de control de flujo.
     */
    private int processControlFlowToken(
            String[] tokens,
            int currentIndex,
            String token,
            Deque<byte[]> stack,
            Deque<Boolean> executionStack
    ) {
        switch (token) {
            case "OP_IF":
                if (shouldExecute(executionStack)) {
                    boolean condition = BytesUtil.castToBool(pop(stack));
                    executionStack.addLast(condition);
                } else {
                    executionStack.addLast(false);
                }
                break;

            case "OP_NOTIF":
                if (shouldExecute(executionStack)) {
                    boolean condition = BytesUtil.castToBool(pop(stack));
                    executionStack.addLast(!condition);
                } else {
                    executionStack.addLast(false);
                }
                break;

            case "OP_ELSE":
                validate(!executionStack.isEmpty(), "OP_ELSE sin bloque OP_IF/OP_NOTIF.");
                boolean currentBranch = executionStack.removeLast();
                boolean parentActive = shouldExecute(executionStack);
                executionStack.addLast(parentActive && !currentBranch);
                break;

            case "OP_ENDIF":
                validate(!executionStack.isEmpty(), "OP_ENDIF sin bloque OP_IF/OP_NOTIF.");
                executionStack.removeLast();
                break;

            default:
                throw new ScriptException("Token de control inválido: " + token);
        }

        trace(token, stack);
        return currentIndex;
    }

    /**
     * Determina si el intérprete debe ejecutar el token actual.
     *
     * @param executionStack pila de condiciones
     * @return true si el bloque actual está activo
     */
    private boolean shouldExecute(Deque<Boolean> executionStack) {
        for (Boolean currentCondition : executionStack) {
            if (!currentCondition) {
                return false;
            }
        }
        return true;
    }

    private boolean isControlFlowToken(String token) {
        return "OP_IF".equals(token)
                || "OP_NOTIF".equals(token)
                || "OP_ELSE".equals(token)
                || "OP_ENDIF".equals(token);
    }

    private boolean isNumericLiteral(String token) {
        return token.matches("-?\\d+");
    }

    private boolean isBracketedData(String token) {
        return token.startsWith("<") && token.endsWith(">") && token.length() >= 2;
    }

    private byte[] parseDataToken(String token) {
        String processedToken = token;

        if (isBracketedData(token)) {
            processedToken = token.substring(1, token.length() - 1);
        }

        if (processedToken.matches("(?i)[0-9a-f]+") && processedToken.length() % 2 == 0) {
            return BytesUtil.fromHex(processedToken);
        }

        return BytesUtil.toBytes(processedToken);
    }

    private byte[] pop(Deque<byte[]> stack) {
        validate(!stack.isEmpty(), "La pila está vacía");
        return stack.removeLast();
    }

    private int popInt(Deque<byte[]> stack) {
        try {
            return BytesUtil.decodeInt(pop(stack));
        } catch (IllegalArgumentException exception) {
            throw new ScriptException("Se esperaba un entero codificado en 4 bytes", exception);
        }
    }

    private void push(Deque<byte[]> stack, byte[] value) {
        stack.addLast(value);
    }

    private void validate(boolean condition, String message) {
        if (!condition) {
            throw new ScriptException(message);
        }
    }

    private void trace(String step, Deque<byte[]> stack) {
        if (!traceEnabled) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(step).append("stack = [");

        boolean first = true;
        for (byte[] element : stack) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(BytesUtil.toReadableString(element));
        }

        builder.append("]");
        System.out.println(builder);
    }
}