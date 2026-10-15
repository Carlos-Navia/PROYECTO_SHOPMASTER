package shopmaster.pagos;

/**
 * Permite al proxy solicitar un procesador despues de validar la compra.
 * Crear un proveedor no debe instanciar el adaptador ni la pasarela.
 */
public interface ProveedorProcesador {

    /**
     * Crea el procesador y sus dependencias bajo demanda.
     * El proxy invocara esta operacion solo tras aprobar los controles.
     *
     * @return procesador de pagos listo para recibir el cobro
     */
    ProcesadorPago crearProcesador();
}
