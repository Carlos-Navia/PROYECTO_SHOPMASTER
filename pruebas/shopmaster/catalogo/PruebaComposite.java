package shopmaster.catalogo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Verificaciones ejecutables sin bibliotecas externas ni entrada por teclado. */
public class PruebaComposite {
    public static void main(String[] argumentos) {
        ElementoCatalogo monitor = new ProductoIndividual("Monitor", 800000);
        ElementoCatalogo teclado = new ProductoIndividual("Teclado", 200000);
        ComboProducto kit = new ComboProducto("Kit de Limpieza");
        kit.agregarComponente(new ProductoIndividual("Liquido limpiador", 25000));
        kit.agregarComponente(new ProductoIndividual("Pano de limpieza", 15000));
        ComboProducto principal = new ComboProducto("Combo Setup Gamer");
        principal.agregarComponente(monitor);
        principal.agregarComponente(teclado);
        principal.agregarComponente(kit);

        verificar(monitor.calcularPrecio() == 800000, "Precio individual");
        verificar(kit.calcularPrecio() == 40000, "Precio del subcombo");
        ElementoCatalogo catalogo = principal;
        verificar(catalogo.calcularPrecio() == 1040000, "Suma recursiva polimorfica");
        verificar(catalogo.obtenerNombre().equals("Combo Setup Gamer"), "Nombre del combo");

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream consola = System.out;
        try (PrintStream captura = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setOut(captura);
            catalogo.mostrarDesglose("");
        } finally {
            System.setOut(consola);
        }
        String desglose = salida.toString(StandardCharsets.UTF_8);
        verificar(desglose.lines().count() == 6, "Todos los nodos del arbol");
        verificar(desglose.contains("  - Monitor:"), "Sangria de producto");
        verificar(desglose.contains("  - Teclado:"), "Desglose de teclado");
        verificar(desglose.contains("  - Kit de Limpieza (combo):"), "Sangria de subcombo");
        verificar(desglose.contains("    - Liquido limpiador:"), "Primer producto anidado");
        verificar(desglose.contains("    - Pano de limpieza:"), "Segundo producto anidado");
        verificar(desglose.contains("$1.040.000,00 COP"), "Formato monetario colombiano");
        System.out.print(desglose);

        verificar(principal.quitarComponente(teclado), "Retiro existente");
        verificar(principal.calcularPrecio() == 840000, "Recalculo tras retiro");
        verificar(!principal.quitarComponente(teclado), "Retiro inexistente");
        principal.agregarComponente(teclado);
        verificar(principal.calcularPrecio() == 1040000, "Reincorporacion");
        verificar(new ComboProducto("Vacio").calcularPrecio() == 0, "Combo vacio");
        verificar(new ProductoIndividual("Gratuito", 0).calcularPrecio() == 0, "Precio cero");

        esperarError(IllegalArgumentException.class, () -> principal.agregarComponente(principal));
        esperarError(IllegalArgumentException.class, () -> kit.agregarComponente(principal));
        verificar(principal.calcularPrecio() == 1040000, "Rechazo de ciclos sin modificar el arbol");
        esperarError(NullPointerException.class, () -> principal.agregarComponente(null));
        esperarError(NullPointerException.class, () -> new ProductoIndividual(null, 1));
        esperarError(IllegalArgumentException.class, () -> new ProductoIndividual(" ", 1));
        esperarError(IllegalArgumentException.class, () -> new ComboProducto(""));
        esperarError(IllegalArgumentException.class, () -> new ProductoIndividual("Invalido", -1));
        esperarError(IllegalArgumentException.class, () -> new ProductoIndividual("Invalido", Double.NaN));
        esperarError(IllegalArgumentException.class,
                () -> new ProductoIndividual("Invalido", Double.POSITIVE_INFINITY));
        System.out.println("Pruebas de Composite superadas.");
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + descripcion);
        }
    }

    private static void esperarError(Class<? extends RuntimeException> tipo, Runnable operacion) {
        try {
            operacion.run();
        } catch (RuntimeException error) {
            verificar(tipo.isInstance(error), "Tipo de excepcion: " + tipo.getSimpleName());
            return;
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }
}
