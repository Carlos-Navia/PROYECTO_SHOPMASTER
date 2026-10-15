package shopmaster.pedido;

import java.util.Locale;
import java.util.Objects;

/** Base de los servicios: contiene otro pedido en lugar de modificarlo. */
public abstract class DecoradorPedido implements CostoPedido {
    protected final CostoPedido pedido;

    protected DecoradorPedido(CostoPedido pedido) {
        this.pedido = Objects.requireNonNull(pedido, "El pedido a decorar es obligatorio.");
    }

    /** Conserva el detalle anterior y agrega el recargo y subtotal de esta capa. */
    protected String describirServicio(String servicio, double recargo) {
        return pedido.obtenerDescripcion() + System.lineSeparator()
                + String.format(Locale.forLanguageTag("es-CO"),
                        "%s: +$%,.2f COP; subtotal: $%,.2f COP",
                        servicio, recargo, calcularCosto());
    }
}
