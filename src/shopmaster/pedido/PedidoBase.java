package shopmaster.pedido;

import java.util.Locale;
import java.util.Objects;
import shopmaster.catalogo.ElementoCatalogo;

/** Conecta un producto o combo del catalogo con el contrato del pedido. */
public class PedidoBase implements CostoPedido {
    private final ElementoCatalogo elemento;

    public PedidoBase(ElementoCatalogo elemento) {
        this.elemento = Objects.requireNonNull(elemento, "El elemento del catalogo es obligatorio.");
    }

    @Override
    public double calcularCosto() {
        return elemento.calcularPrecio();
    }

    @Override
    public String obtenerDescripcion() {
        return String.format(Locale.forLanguageTag("es-CO"),
                "Pedido base: %s - $%,.2f COP", elemento.obtenerNombre(), calcularCosto());
    }
}
