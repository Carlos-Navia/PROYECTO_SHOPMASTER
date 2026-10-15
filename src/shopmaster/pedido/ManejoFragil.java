package shopmaster.pedido;

/** Agrega el recargo fijo por manejo prioritario fragil. */
public class ManejoFragil extends DecoradorPedido {
    private static final double RECARGO = 25000;

    public ManejoFragil(CostoPedido pedido) {
        super(pedido);
    }

    @Override
    public double calcularCosto() {
        return pedido.calcularCosto() + RECARGO;
    }

    @Override
    public String obtenerDescripcion() {
        return describirServicio("Manejo Prioritario Fragil", RECARGO);
    }
}
