package shopmaster.pedido;

/**
 * Contrato compartido por el pedido base y los servicios del patron Decorator.
 */
public interface CostoPedido {

    /**
     * Calcula el costo en COP, incluidos los servicios aplicados.
     * Cada garantia afecta el valor acumulado del objeto que envuelve.
     *
     * @return costo total en pesos colombianos
     */
    double calcularCosto();

    /**
     * Describe el pedido y los servicios en su orden de aplicacion.
     *
     * @return descripcion del pedido con sus servicios
     */
    String obtenerDescripcion();
}
