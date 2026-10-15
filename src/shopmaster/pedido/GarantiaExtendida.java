package shopmaster.pedido;

/** Agrega el diez por ciento del costo acumulado del pedido que envuelve. */
public class GarantiaExtendida extends DecoradorPedido {
    private static final double PORCENTAJE = 0.10;

    public GarantiaExtendida(CostoPedido pedido) {
        super(pedido);
    }

    @Override
    public double calcularCosto() {
        double costoActual = pedido.calcularCosto();
        return costoActual + costoActual * PORCENTAJE;
    }

    @Override
    public String obtenerDescripcion() {
        return describirServicio("Poliza de Garantia Extendida (10 %)",
                pedido.calcularCosto() * PORCENTAJE);
    }
}
