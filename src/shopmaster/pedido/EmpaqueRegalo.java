package shopmaster.pedido;

/** Cada envoltura agrega un empaque; puede aplicarse varias veces. */
public class EmpaqueRegalo extends DecoradorPedido {
    private static final double RECARGO = 15000;

    public EmpaqueRegalo(CostoPedido pedido) {
        super(pedido);
    }

    @Override
    public double calcularCosto() {
        return pedido.calcularCosto() + RECARGO;
    }

    @Override
    public String obtenerDescripcion() {
        return describirServicio("Empaque de Regalo Premium", RECARGO);
    }
}
