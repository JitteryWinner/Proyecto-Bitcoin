package proyecto.bitcoin;

/**
 * Clase principal para demostrar el funcionamiento del intérprete.
 */
public class ScriptMain {

    /**
     * Punto de entrada del programa.
     *
     * @param args argumentos de consola
     */
    public static void main(String[] args) {
        boolean traceEnabled = true;
        ScriptEngine scriptEngine = new ScriptEngine(traceEnabled);

        String publicKey = "PUBKEY_ALICE";
        String validSignature = "SIG(" + publicKey + ")";
        String invalidSignature = "SIG(PUBKEY_FAKE)";

        byte[] publicKeyHash = CryptoMock.hash160(BytesUtil.toBytes(publicKey));
        String publicKeyHashHex = BytesUtil.toHex(publicKeyHash);

        String scriptPubKey = "OP_DUP OP_HASH160 PUSHDATA " + publicKeyHashHex + " OP_EQUALVERIFY OP_CHECKSIG";
        String validScriptSig = "<" + validSignature + "> <" + publicKey + ">";
        String invalidScriptSig = "<" + invalidSignature + "> <" + publicKey + ">";

        String validProgram = validScriptSig + " " + scriptPubKey;
        String invalidProgram = invalidScriptSig + " " + scriptPubKey;

        System.out.println(" DEMO P2PKH VÁLIDO");
        boolean validResult = scriptEngine.eval(validProgram);
        System.out.println("Resultado final: " + (validResult ? "VÁLIDO" : "INVÁLIDO"));

        System.out.println("\n DEMO P2PKH INVÁLIDO");
        try {
            boolean invalidResult = scriptEngine.eval(invalidProgram);
            System.out.println("Resultado final: " + (invalidResult ? "VÁLIDO" : "INVÁLIDO"));
        } catch (ScriptException exception) {
            System.out.println("La ejecución falló como se esperaba: " + exception.getMessage());
        }

        System.out.println("\n DEMO CONDICIONAL");
        String conditionalProgram = "1 OP_IF 10 5 OP_ADD 15 OP_NUMEQUALVERIFY OP_ELSE 0 OP_ENDIF 1";
        boolean conditionalResult = scriptEngine.eval(conditionalProgram);
        System.out.println("Resultado final del condicional: " + (conditionalResult ? "VÁLIDO" : "INVÁLIDO"));
    }
}