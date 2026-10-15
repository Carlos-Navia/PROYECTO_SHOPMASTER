package shopmaster.excepciones;

/** Indica que los controles de seguridad impidieron solicitar el cobro. */
public class PagoRechazadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PagoRechazadoException(String mensaje) {
        super(mensaje);
    }
}
