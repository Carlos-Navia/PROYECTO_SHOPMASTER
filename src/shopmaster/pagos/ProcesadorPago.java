package shopmaster.pagos;

/**
 * Contrato local compartido por el adaptador de pagos y el proxy de seguridad.
 */
public interface ProcesadorPago {

    /**
     * Solicita un cobro expresado en pesos colombianos.
     * La operacion retorna normalmente cuando el cobro termina correctamente;
     * los rechazos y fallos se comunican mediante excepciones.
     *
     * @param usuario identificador del cliente
     * @param montoCOP importe del cobro en pesos colombianos
     */
    void cobrarEnPesos(String usuario, double montoCOP);
}
