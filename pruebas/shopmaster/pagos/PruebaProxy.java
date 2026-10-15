package shopmaster.pagos;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import shopmaster.excepciones.PagoRechazadoException;

/** Comprueba seguridad, creacion diferida y auditoria con dependencias de prueba. */
public class PruebaProxy {
    public static void main(String[] argumentos) {
        ProveedorRegistro proveedor = new ProveedorRegistro();
        ProcesadorPago proxy = new ProxyPagoSeguro(proveedor);
        verificar(proveedor.creaciones == 0, "Construir el proxy no crea el banco");

        String rechazos = capturar(() -> {
            esperarRechazo(() -> proxy.cobrarEnPesos("cliente", 6025000), "limite");
            esperarRechazo(() -> proxy.cobrarEnPesos("BLOQUEADO", 150000), "BLOQUEADO");
            esperarRechazo(() -> proxy.cobrarEnPesos("cliente", 5000000.01), "limite");
            esperarRechazo(() -> proxy.cobrarEnPesos(null, 100), "usuario");
            esperarRechazo(() -> proxy.cobrarEnPesos(" ", 100), "usuario");
            esperarRechazo(() -> proxy.cobrarEnPesos("cliente", -1), "monto");
            esperarRechazo(() -> proxy.cobrarEnPesos("cliente", Double.NaN), "monto");
            esperarRechazo(() -> proxy.cobrarEnPesos("cliente", Double.POSITIVE_INFINITY), "monto");
        });
        verificar(proveedor.creaciones == 0, "Cero procesadores creados en rechazos");
        verificar(proveedor.pasarelasCreadas == 0, "Cero pasarelas creadas en rechazos");
        verificar(proveedor.cobros == 0, "Cero llamadas externas en rechazos");
        verificar(!rechazos.contains("AUDITORÍA"), "No auditar aprobacion en rechazos");

        String aprobada = capturar(() -> proxy.cobrarEnPesos("cliente_vip", 1159000));
        verificar(proveedor.creaciones == 1 && proveedor.pasarelasCreadas == 1,
                "Crear dependencias solo al aprobar");
        verificar(proveedor.cobros == 1, "Un cobro externo");
        verificar(Math.abs(proveedor.montoUSD - 289.75) < 0.000001, "Conversion tras validar COP");
        verificar("cliente_vip".equals(proveedor.usuario), "Usuario delegado");
        verificar("clave-pruebas".equals(proveedor.clave), "Configuracion delegada");
        verificar(aprobada.contains("[AUDITORÍA: Transacción aprobada para cliente_vip"),
                "Formato de auditoria");
        verificar(aprobada.indexOf("COBRO_SIMULADO") < aprobada.indexOf("AUDITORÍA"),
                "Auditoria final tras completar el cobro");

        capturar(() -> proxy.cobrarEnPesos("cliente", 5000000));
        verificar(proveedor.montoUSD == 1250, "Limite exacto permitido");
        capturar(() -> proxy.cobrarEnPesos("bloqueado", 0));
        verificar(proveedor.cobros == 3, "Comparacion exacta de usuario y monto cero permitido");
        capturar(() -> esperarRechazo(() -> proxy.cobrarEnPesos("BLOQUEADO", 150000), "BLOQUEADO"));
        verificar(proveedor.creaciones == 3 && proveedor.cobros == 3,
                "Rechazo posterior a compras validas no crea ni cobra");

        proveedor.fallo = new IllegalStateException("Fallo de pasarela simulado.");
        String fallida = capturar(() -> {
            try {
                proxy.cobrarEnPesos("cliente", 4000);
                throw new AssertionError("El error bancario debe propagarse.");
            } catch (IllegalStateException error) {
                verificar(error == proveedor.fallo, "Preservar el fallo bancario");
            }
        });
        verificar(!fallida.contains("AUDITORÍA"), "No auditar exito si falla el banco");
        verificar(proveedor.creaciones == 4 && proveedor.cobros == 4, "Sin reintentos del cobro fallido");

        ProveedorStripe proveedorStripe = new ProveedorStripe("clave-simulacion");
        String integrada = capturar(() -> new ProxyPagoSeguro(proveedorStripe)
                .cobrarEnPesos("cliente_vip", 1159000));
        verificar(integrada.contains("StripeUSA (simulacion)"), "Proveedor real conectado al adaptador");
        verificar(integrada.contains("AUDITORÍA"), "Auditoria de la cadena completa");
        System.out.print(rechazos);
        System.out.print(integrada);
        System.out.println("Pruebas de Proxy superadas: cero creaciones y cobros en rechazos.");
    }

    private static class ProveedorRegistro implements ProveedorProcesador {
        private int creaciones;
        private int pasarelasCreadas;
        private int cobros;
        private double montoUSD;
        private String usuario;
        private String clave;
        private IllegalStateException fallo;

        @Override
        public ProcesadorPago crearProcesador() {
            creaciones++;
            StripeUSA pasarela = new StripeUSA() {
                @Override
                public void chargeInDollars(double amountUSD, String customerEmail, String merchantKey) {
                    cobros++;
                    montoUSD = amountUSD;
                    usuario = customerEmail;
                    clave = merchantKey;
                    if (fallo != null) {
                        throw fallo;
                    }
                    System.out.println("COBRO_SIMULADO");
                }
            };
            pasarelasCreadas++;
            return new AdaptadorStripe(pasarela, "clave-pruebas");
        }
    }

    private static String capturar(Runnable operacion) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream consola = System.out;
        try (PrintStream captura = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setOut(captura);
            operacion.run();
        } finally {
            System.setOut(consola);
        }
        return salida.toString(StandardCharsets.UTF_8);
    }

    private static void esperarRechazo(Runnable operacion, String motivo) {
        try {
            operacion.run();
        } catch (PagoRechazadoException error) {
            verificar(error.getMessage().contains(motivo), "Motivo del rechazo");
            return;
        }
        throw new AssertionError("Se esperaba un rechazo de seguridad.");
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + descripcion);
        }
    }
}
