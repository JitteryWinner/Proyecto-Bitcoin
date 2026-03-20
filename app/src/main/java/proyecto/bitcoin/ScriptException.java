package proyecto.bitcoin;

/**
 * Excepción específica para errores durante la ejecución de Bitcoin Script.
 */
public class ScriptException extends RuntimeException {

    /**
     * Crea una nueva excepción de script.
     *
     * @param message mensaje descriptivo del error
     */
    public ScriptException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción de script con causa interna.
     *
     * @param message mensaje descriptivo del error
     * @param cause causa original
     */
    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}