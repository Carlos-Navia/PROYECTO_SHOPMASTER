package shopmaster.compra;

/** Resultado del intento de pago coordinado por la fachada. */
public enum EstadoCompra {
    EXITOSA,
    RECHAZADA,
    ERROR_PASARELA
}
