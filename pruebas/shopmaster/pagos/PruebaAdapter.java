package shopmaster.pagos;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Verifica la adaptacion con una pasarela de prueba que registra las llamadas. */
public class PruebaAdapter {
    public static void main(String[] argumentos) {
        PasarelaRegistro pasarela = new PasarelaRegistro();
        ProcesadorPago adaptador = new AdaptadorStripe(pasarela, "clave-solo-pruebas");
        adaptador.cobrarEnPesos("cliente_vip", 1159000);
        verificar(pasarela.llamadas == 1, "Una llamada externa por cobro");
        verificar(Math.abs(pasarela.montoRecibido - 289.75) < 0.000001, "Conversion del caso oficial");
        verificar("cliente_vip".equals(pasarela.usuarioRecibido), "Identificador sin alteraciones");
        verificar("clave-solo-pruebas".equals(pasarela.claveRecibida), "Clave configurada");

        adaptador.cobrarEnPesos("otro_cliente", 1160500);
        verificar(Math.abs(pasarela.montoRecibido - 290.125) < 0.000001,
                "No redondear el importe antes de enviarlo");
        adaptador.cobrarEnPesos("otro_cliente", 0);
        verificar(pasarela.montoRecibido == 0, "Monto cero coherente con el catalogo");

        esperarError(IllegalArgumentException.class, () -> adaptador.cobrarEnPesos("cliente", -1));
        esperarError(IllegalArgumentException.class, () -> adaptador.cobrarEnPesos("cliente", Double.NaN));
        esperarError(IllegalArgumentException.class,
                () -> adaptador.cobrarEnPesos("cliente", Double.POSITIVE_INFINITY));
        esperarError(IllegalArgumentException.class, () -> adaptador.cobrarEnPesos(" ", 100));
        esperarError(NullPointerException.class, () -> adaptador.cobrarEnPesos(null, 100));
        verificar(pasarela.llamadas == 3, "Datos invalidos no llegan a la pasarela");
        esperarError(NullPointerException.class, () -> new AdaptadorStripe(null, "clave"));
        esperarError(NullPointerException.class, () -> new AdaptadorStripe(pasarela, null));
        esperarError(IllegalArgumentException.class, () -> new AdaptadorStripe(pasarela, " "));

        IllegalStateException fallo = new IllegalStateException("Fallo simulado de la pasarela.");
        pasarela.fallo = fallo;
        try {
            adaptador.cobrarEnPesos("cliente", 4000);
            throw new AssertionError("El fallo de la pasarela debe propagarse.");
        } catch (IllegalStateException error) {
            verificar(error == fallo, "Conservar el error original");
        }
        verificar(pasarela.llamadas == 4, "Una sola llamada en el fallo, sin reintentos");

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream consola = System.out;
        try (PrintStream captura = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setOut(captura);
            new AdaptadorStripe(new StripeUSA(), "clave-no-imprimir")
                    .cobrarEnPesos("cliente_vip", 1159000);
        } finally {
            System.setOut(consola);
        }
        String detalle = salida.toString(StandardCharsets.UTF_8);
        verificar(detalle.contains("1.159.000,00 COP"), "Monto original visible");
        verificar(detalle.contains("289.75"), "Monto convertido visible");
        verificar(detalle.contains("StripeUSA (simulacion)"), "Simulacion identificada");
        verificar(!detalle.contains("clave-no-imprimir"), "No exponer la clave comercial");
        System.out.print(detalle);
        System.out.println("Pruebas de Adapter superadas.");
    }

    private static class PasarelaRegistro extends StripeUSA {
        private int llamadas;
        private double montoRecibido;
        private String usuarioRecibido;
        private String claveRecibida;
        private IllegalStateException fallo;

        @Override
        public void chargeInDollars(double amountUSD, String customerEmail, String merchantKey) {
            llamadas++;
            montoRecibido = amountUSD;
            usuarioRecibido = customerEmail;
            claveRecibida = merchantKey;
            if (fallo != null) {
                throw fallo;
            }
        }
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
