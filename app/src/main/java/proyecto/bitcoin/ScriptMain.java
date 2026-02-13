package proyecto.bitcoin;


public class ScriptMain {

    public static void main(String[] args) {
        boolean trace = true;

        // Creamos un pubKey y su "firma" valida simulada
        String pubKey = "PUBKEY_ALICE";
        String sigOk = "SIG(" + pubKey + ")";

        // pubKeyHash = OP_HASH160(pubKey) pero lo calculamos aqui para armar el scriptPubKey
        byte[] pubKeyHash = CryptoMock.hash160Mock(BytesUtil.bytes(pubKey));

        // Para prototipo simple, lo serializamos como Base16 mini (hex) para poder ponerlo en el script.
        String pubKeyHashHex = BytesUtil.toHex(pubKeyHash);

        // scriptSig: <firma> <pubKey>
        String scriptSig = "<" + sigOk + "> <" + pubKey + ">";

        // scriptPubKey estilo P2PKH:
        // OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
        String scriptPubKey = "OP_DUP OP_HASH160 PUSHDATA " + pubKeyHashHex + " OP_EQUALVERIFY OP_CHECKSIG";

        // Programa combinado: scriptSig + scriptPubKey
        String program = scriptSig + " " + scriptPubKey;

        System.out.println("Programa (scriptSig + scriptPubKey)");
        System.out.println(program);
        System.out.println("\nTrace");

        boolean ok = ScriptEngine.eval(program, trace);
        System.out.println("\nResultado Final: " + (ok ? "Valido (Verdadero)" : "Invalido (Falso)"));

        // Prueba negativa rapida: firma incorrecta
        System.out.println("\nPrueba NEGATIVA (firma mala)");
        String badSig = "SIG(PUBKEY_OTRO)";
        String programBad = "<" + badSig + "> <" + pubKey + "> " + scriptPubKey;

        boolean okBad = false;
        try {
            okBad = ScriptEngine.eval(programBad, trace);
        } catch (Exception e) {
            System.out.println("Fallo que se esperaba: " + e.getMessage());
        }
        System.out.println("Resultado Negativo: " + (okBad ? "Valido" : "Invalido"));
    }
}
