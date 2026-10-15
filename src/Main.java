import java.util.List;
import shopmaster.compra.DefinicionProducto;
import shopmaster.compra.FachadaCompra;
import shopmaster.compra.TipoServicio;

/** Ejecuta los tres escenarios oficiales sin solicitar datos por teclado. */
public class Main {
    public static void main(String[] argumentos) {
        // Configuracion de demostracion; no es una credencial de Stripe real.
        String claveComercio = "clave-shopmaster-simulacion";
        FachadaCompra fachada = new FachadaCompra(claveComercio);

        System.out.println("CASO 1: Compra exitosa completa");
        DefinicionProducto kitLimpieza = DefinicionProducto.crearCombo("Kit de Limpieza", List.of(
                DefinicionProducto.crearIndividual("Liquido limpiador", 25000),
                DefinicionProducto.crearIndividual("Pano de limpieza", 15000)));
        DefinicionProducto comboGamer = DefinicionProducto.crearCombo("Combo Setup Gamer", List.of(
                DefinicionProducto.crearIndividual("Monitor", 800000),
                DefinicionProducto.crearIndividual("Teclado", 200000),
                kitLimpieza));
        // El orden es explicito: primero garantia sobre la base y despues empaque.
        fachada.ejecutarCompraRapida("cliente_vip", comboGamer,
                List.of(TipoServicio.GARANTIA, TipoServicio.EMPAQUE));

        System.out.println();
        System.out.println("CASO 2: Rechazo por tope de dinero");
        DefinicionProducto servidor = DefinicionProducto.crearIndividual("Servidor Empresarial", 6000000);
        fachada.ejecutarCompraRapida("cliente_normal", servidor, List.of(TipoServicio.MANEJO_FRAGIL));

        System.out.println();
        System.out.println("CASO 3: Rechazo por usuario reportado");
        DefinicionProducto mouse = DefinicionProducto.crearIndividual("Mouse", 150000);
        fachada.ejecutarCompraRapida("BLOQUEADO", mouse, List.of());
    }
}
