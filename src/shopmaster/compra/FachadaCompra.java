package shopmaster.compra;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import shopmaster.catalogo.ComboProducto;
import shopmaster.catalogo.ElementoCatalogo;
import shopmaster.catalogo.ProductoIndividual;
import shopmaster.excepciones.PagoRechazadoException;
import shopmaster.pagos.ProcesadorPago;
import shopmaster.pagos.ProveedorStripe;
import shopmaster.pagos.ProxyPagoSeguro;
import shopmaster.pedido.CostoPedido;
import shopmaster.pedido.EmpaqueRegalo;
import shopmaster.pedido.GarantiaExtendida;
import shopmaster.pedido.ManejoFragil;
import shopmaster.pedido.PedidoBase;

/** Punto de entrada que coordina el catalogo, los servicios y el pago seguro. */
public class FachadaCompra {
    private final ProcesadorPago procesadorSeguro;

    /** Configura el subsistema sin crear todavia las dependencias bancarias. */
    public FachadaCompra(String claveComercio) {
        this(new ProxyPagoSeguro(new ProveedorStripe(claveComercio)));
    }

    /** Permite proporcionar un proxy configurado o una dependencia de prueba. */
    public FachadaCompra(ProcesadorPago procesadorSeguro) {
        this.procesadorSeguro = Objects.requireNonNull(procesadorSeguro,
                "El procesador seguro es obligatorio.");
    }

    /**
     * Compra a partir de datos; aplica servicios en el orden recibido.
     * Los datos de catalogo o servicios invalidos se notifican con excepciones.
     * Los rechazos y errores del pago se traducen a un estado de compra.
     */
    public EstadoCompra ejecutarCompraRapida(String usuario, DefinicionProducto definicion,
            List<TipoServicio> servicios) {
        Objects.requireNonNull(definicion, "La definicion del producto es obligatoria.");
        List<TipoServicio> serviciosOrdenados = List.copyOf(
                Objects.requireNonNull(servicios, "La lista de servicios es obligatoria."));
        ElementoCatalogo elemento = construirCatalogo(definicion);
        CostoPedido pedido = aplicarServicios(elemento, serviciosOrdenados);

        System.out.println("Compra para: " + usuario);
        System.out.println("Arbol de productos:");
        elemento.mostrarDesglose("");
        System.out.println(pedido.obtenerDescripcion());
        double totalCOP = pedido.calcularCosto();
        System.out.printf(Locale.forLanguageTag("es-CO"), "Total del pedido: $%,.2f COP%n", totalCOP);

        EstadoCompra estado;
        try {
            procesadorSeguro.cobrarEnPesos(usuario, totalCOP);
            estado = EstadoCompra.EXITOSA;
        } catch (PagoRechazadoException error) {
            // El proxy ya ha informado el motivo especifico del rechazo.
            estado = EstadoCompra.RECHAZADA;
        } catch (RuntimeException error) {
            System.out.println("Error al procesar el pago: " + error.getMessage());
            estado = EstadoCompra.ERROR_PASARELA;
        }
        System.out.println("Resultado de la compra: " + estado);
        return estado;
    }

    private ElementoCatalogo construirCatalogo(DefinicionProducto definicion) {
        if (!definicion.esCombo()) {
            return new ProductoIndividual(definicion.obtenerNombre(), definicion.obtenerPrecioIndividual());
        }
        ComboProducto combo = new ComboProducto(definicion.obtenerNombre());
        for (DefinicionProducto componente : definicion.obtenerComponentes()) {
            combo.agregarComponente(construirCatalogo(componente));
        }
        return combo;
    }

    private CostoPedido aplicarServicios(ElementoCatalogo elemento, List<TipoServicio> servicios) {
        CostoPedido pedido = new PedidoBase(elemento);
        for (TipoServicio servicio : servicios) {
            switch (servicio) {
                case GARANTIA:
                    pedido = new GarantiaExtendida(pedido);
                    break;
                case EMPAQUE:
                    pedido = new EmpaqueRegalo(pedido);
                    break;
                case MANEJO_FRAGIL:
                    pedido = new ManejoFragil(pedido);
                    break;
                default:
                    throw new IllegalArgumentException("Servicio no reconocido: " + servicio);
            }
        }
        return pedido;
    }
}
