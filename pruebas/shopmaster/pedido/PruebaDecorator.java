package shopmaster.pedido;

import shopmaster.catalogo.ComboProducto;
import shopmaster.catalogo.ProductoIndividual;

/** Comprueba importes y orden de los servicios sin bibliotecas externas. */
public class PruebaDecorator {
    public static void main(String[] argumentos) {
        ComboProducto kit = new ComboProducto("Kit de Limpieza");
        kit.agregarComponente(new ProductoIndividual("Liquido limpiador", 25000));
        kit.agregarComponente(new ProductoIndividual("Pano de limpieza", 15000));
        ComboProducto combo = new ComboProducto("Combo Setup Gamer");
        combo.agregarComponente(new ProductoIndividual("Monitor", 800000));
        combo.agregarComponente(new ProductoIndividual("Teclado", 200000));
        combo.agregarComponente(kit);
        CostoPedido base = new PedidoBase(combo);

        verificarCosto(base, 1040000, "Pedido sin servicios");
        verificarCosto(new GarantiaExtendida(base), 1144000, "Solo garantia");
        verificarCosto(new EmpaqueRegalo(base), 1055000, "Solo empaque");
        verificarCosto(new ManejoFragil(base), 1065000, "Solo manejo fragil");
        verificarCosto(new EmpaqueRegalo(new EmpaqueRegalo(base)), 1070000, "Doble empaque");

        CostoPedido garantiaLuegoEmpaque = new EmpaqueRegalo(new GarantiaExtendida(base));
        CostoPedido empaqueLuegoGarantia = new GarantiaExtendida(new EmpaqueRegalo(base));
        verificarCosto(garantiaLuegoEmpaque, 1159000, "Garantia y despues empaque");
        verificarCosto(empaqueLuegoGarantia, 1160500, "Empaque y despues garantia");
        verificarCosto(new ManejoFragil(garantiaLuegoEmpaque), 1184000, "Tres servicios");
        verificarCosto(new GarantiaExtendida(new ManejoFragil(new EmpaqueRegalo(base))),
                1188000, "Garantia sobre ambos recargos fijos");
        verificarCosto(new EmpaqueRegalo(new GarantiaExtendida(new ManejoFragil(base))),
                1186500, "Garantia entre recargos fijos");
        verificarCosto(base, 1040000, "Decorar no modifica el pedido base");

        CostoPedido individual = new PedidoBase(new ProductoIndividual("Servidor Empresarial", 6000000));
        verificarCosto(new ManejoFragil(individual), 6025000, "Producto individual con servicio");

        String descripcion = garantiaLuegoEmpaque.obtenerDescripcion();
        verificar(descripcion.contains("Combo Setup Gamer"), "Conservar nombre del pedido");
        verificar(descripcion.contains("+$104.000,00 COP"), "Recargo porcentual visible");
        verificar(descripcion.contains("+$15.000,00 COP"), "Recargo fijo visible");
        verificar(descripcion.contains("subtotal: $1.159.000,00 COP"), "Total visible");
        verificar(descripcion.indexOf("Garantia") < descripcion.indexOf("Empaque"),
                "Descripcion en orden de aplicacion");
        verificar(empaqueLuegoGarantia.obtenerDescripcion().contains("+$105.500,00 COP"),
                "Garantia calculada sobre el empaque previo");
        String dobleEmpaque = new EmpaqueRegalo(new EmpaqueRegalo(base)).obtenerDescripcion();
        verificar(dobleEmpaque.lines().filter(linea -> linea.contains("Empaque de Regalo")).count() == 2,
                "Ambos empaques aparecen en el detalle");

        esperarNulo(() -> new PedidoBase(null));
        esperarNulo(() -> new GarantiaExtendida(null));
        esperarNulo(() -> new EmpaqueRegalo(null));
        esperarNulo(() -> new ManejoFragil(null));

        System.out.println(descripcion);
        System.out.println("Pruebas de Decorator superadas.");
    }

    private static void verificarCosto(CostoPedido pedido, double esperado, String descripcion) {
        verificar(Math.abs(pedido.calcularCosto() - esperado) < 0.000001, descripcion);
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + descripcion);
        }
    }

    private static void esperarNulo(Runnable operacion) {
        try {
            operacion.run();
        } catch (NullPointerException error) {
            return;
        }
        throw new AssertionError("Se esperaba rechazo de una dependencia nula.");
    }
}
