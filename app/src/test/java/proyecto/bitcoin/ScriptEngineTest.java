package proyecto.bitcoin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Casos de prueba para el intérprete de Bitcoin Script.
 */
public class ScriptEngineTest {

    @Test
    public void deberiaValidarP2PKHCorrecto() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        String publicKey = "PUBKEY_ALICE";
        String signature = "SIG(" + publicKey + ")";
        String publicKeyHashHex = BytesUtil.toHex(CryptoMock.hash160(BytesUtil.toBytes(publicKey)));

        String scriptSig = "<" + signature + "> <" + publicKey + ">";
        String scriptPubKey = "OP_DUP OP_HASH160 PUSHDATA " + publicKeyHashHex + " OP_EQUALVERIFY OP_CHECKSIG";

        boolean result = scriptEngine.eval(scriptSig + " " + scriptPubKey);

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaFallarFirmaIncorrectaEnP2PKH() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        String publicKey = "PUBKEY_ALICE";
        String wrongSignature = "SIG(PUBKEY_OTRO)";
        String publicKeyHashHex = BytesUtil.toHex(CryptoMock.hash160(BytesUtil.toBytes(publicKey)));

        String scriptSig = "<" + wrongSignature + "> <" + publicKey + ">";
        String scriptPubKey = "OP_DUP OP_HASH160 PUSHDATA " + publicKeyHashHex + " OP_EQUALVERIFY OP_CHECKSIG";

        boolean result = scriptEngine.eval(scriptSig + " " + scriptPubKey);

        Assertions.assertFalse(result);
    }

    @Test
    public void deberiaEjecutarSumaCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("2 3 OP_ADD 5 OP_EQUAL");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEjecutarRestaCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("8 3 OP_SUB 5 OP_EQUAL");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEvaluarMayorQueCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("5 3 OP_GREATERTHAN");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEvaluarMenorQueCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("3 7 OP_LESSTHAN");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEjecutarAndBooleanoCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("1 1 OP_BOOLAND");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEjecutarOrBooleanoCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("0 1 OP_BOOLOR");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEjecutarRamaIfCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("1 OP_IF 1 OP_ELSE 0 OP_ENDIF");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEjecutarRamaElseCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("0 OP_IF 0 OP_ELSE 1 OP_ENDIF");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEjecutarCondicionalesAnidados() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        String program = "1 OP_IF 1 OP_IF 1 OP_ELSE 0 OP_ENDIF OP_ELSE 0 OP_ENDIF";
        boolean result = scriptEngine.eval(program);

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaFallarSiLaPilaEstaVaciaEnDup() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        Assertions.assertThrows(ScriptException.class, () -> scriptEngine.eval("OP_DUP"));
    }

    @Test
    public void deberiaFallarVerifyCuandoEsFalso() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        Assertions.assertThrows(ScriptException.class, () -> scriptEngine.eval("0 OP_VERIFY"));
    }

    @Test
    public void deberiaFallarConReturn() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        Assertions.assertThrows(ScriptException.class, () -> scriptEngine.eval("1 OP_RETURN"));
    }

    @Test
    public void deberiaFallarSiElIfNoSeCierra() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        Assertions.assertThrows(ScriptException.class, () -> scriptEngine.eval("1 OP_IF 1"));
    }

    @Test
    public void deberiaEjecutarOperacionesHash() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        boolean result = scriptEngine.eval("<hola> OP_SHA256 OP_DUP OP_EQUAL");

        Assertions.assertTrue(result);
    }

    @Test
    public void deberiaEjecutarCheckSigVerifyCorrectamente() {
        ScriptEngine scriptEngine = new ScriptEngine(false);

        String publicKey = "PUBKEY_BOB";
        String signature = "SIG(" + publicKey + ")";

        boolean result = scriptEngine.eval("<" + signature + "> <" + publicKey + "> OP_CHECKSIGVERIFY 1");

        Assertions.assertTrue(result);
    }
}
