import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Comprueba los tres escenarios reales de entrada y sus salidas observables. */
public class PruebaMain {
    public static void main(String[] argumentos) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream consola = System.out;
        try (PrintStream captura = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setOut(captura);
            Main.main(new String[0]);
        } finally {
            System.setOut(consola);
        }
        String detalle = salida.toString(StandardCharsets.UTF_8);
        int caso1 = detalle.indexOf("CASO 1:");
        int caso2 = detalle.indexOf("CASO 2:");
        int caso3 = detalle.indexOf("CASO 3:");
        verificar(caso1 >= 0 && caso2 > caso1 && caso3 > caso2, "Tres casos en orden");
        String exitosa = detalle.substring(caso1, caso2);
        String montoRechazado = detalle.substring(caso2, caso3);
        String usuarioRechazado = detalle.substring(caso3);

        verificar(exitosa.contains("Combo Setup Gamer") && exitosa.contains("    - Pano de limpieza"),
                "Arbol anidado en Main");
        verificar(exitosa.contains("Total del pedido: $1.159.000,00 COP"), "Total del caso 1");
        verificar(exitosa.contains("USD 289.75"), "Conversion del caso 1");
        verificar(exitosa.contains("[AUDITORÍA: Transacción aprobada para cliente_vip"),
                "Auditoria del caso 1");
        verificar(contar(detalle, "StripeUSA (simulacion)") == 1, "Un solo cobro externo");
        verificar(contar(detalle, "Resultado de la compra: EXITOSA") == 1, "Una compra exitosa");
        verificar(contar(detalle, "Resultado de la compra: RECHAZADA") == 2, "Dos compras rechazadas");
        verificar(montoRechazado.contains("Total del pedido: $6.025.000,00 COP")
                && montoRechazado.contains("supera el limite"), "Rechazo del caso 2");
        verificar(usuarioRechazado.contains("Total del pedido: $150.000,00 COP")
                && usuarioRechazado.contains("usuario BLOQUEADO esta reportado"), "Rechazo del caso 3");
        String rechazos = montoRechazado + usuarioRechazado;
        verificar(!rechazos.contains("StripeUSA") && !rechazos.contains("Conversion")
                && !rechazos.contains("AUDITORÍA"), "Rechazos sin conversion, cobro ni aprobacion");
        verificar(!detalle.contains("clave-shopmaster-simulacion"), "Clave fuera de la salida");
        System.out.println("Pruebas de Main superadas: tres escenarios oficiales verificados.");
    }

    private static int contar(String texto, String fragmento) {
        int cantidad = 0;
        int posicion = texto.indexOf(fragmento);
        while (posicion >= 0) {
            cantidad++;
            posicion = texto.indexOf(fragmento, posicion + fragmento.length());
        }
        return cantidad;
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + descripcion);
        }
    }
}
