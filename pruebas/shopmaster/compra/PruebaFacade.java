package shopmaster.compra;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import shopmaster.pagos.ProcesadorPago;
import shopmaster.pagos.ProveedorProcesador;
import shopmaster.pagos.ProxyPagoSeguro;

/** Verifica la coordinacion de compras y su aislamiento entre solicitudes. */
public class PruebaFacade {
    public static void main(String[] argumentos) {
        DefinicionProducto kit = DefinicionProducto.crearCombo("Kit de Limpieza", List.of(
                DefinicionProducto.crearIndividual("Liquido limpiador", 25000),
                DefinicionProducto.crearIndividual("Pano de limpieza", 15000)));
        DefinicionProducto combo = DefinicionProducto.crearCombo("Combo Setup Gamer", List.of(
                DefinicionProducto.crearIndividual("Monitor", 800000),
                DefinicionProducto.crearIndividual("Teclado", 200000), kit));
        ProveedorRegistro proveedor = new ProveedorRegistro();
        FachadaCompra fachada = new FachadaCompra(new ProxyPagoSeguro(proveedor));

        String exitosa = capturar(() -> verificar(fachada.ejecutarCompraRapida("cliente_vip", combo,
                List.of(TipoServicio.GARANTIA, TipoServicio.EMPAQUE)) == EstadoCompra.EXITOSA,
                "Compra completa exitosa"));
        verificar(proveedor.monto == 1159000 && proveedor.creaciones == 1 && proveedor.cobros == 1,
                "Delegar el total final una sola vez");
        verificar("cliente_vip".equals(proveedor.usuario), "Conservar el usuario");
        verificar(exitosa.contains("    - Pano de limpieza"), "Construccion de subcombo");
        verificar(exitosa.contains("+$104.000,00 COP"), "Detalle de garantia");
        verificar(exitosa.contains("Total del pedido: $1.159.000,00 COP"), "Total visible");

        capturar(() -> verificar(fachada.ejecutarCompraRapida("cliente", combo,
                List.of(TipoServicio.EMPAQUE, TipoServicio.GARANTIA)) == EstadoCompra.EXITOSA,
                "Orden inverso permitido"));
        verificar(proveedor.monto == 1160500, "Respetar orden inverso");
        capturar(() -> fachada.ejecutarCompraRapida("cliente", combo,
                List.of(TipoServicio.EMPAQUE, TipoServicio.EMPAQUE)));
        verificar(proveedor.monto == 1070000, "Doble empaque");
        capturar(() -> fachada.ejecutarCompraRapida("cliente", combo, List.of()));
        verificar(proveedor.monto == 1040000, "Una compra anterior no deja servicios aplicados");

        DefinicionProducto servidor = DefinicionProducto.crearIndividual("Servidor Empresarial", 6000000);
        DefinicionProducto mouse = DefinicionProducto.crearIndividual("Mouse", 150000);
        String rechazos = capturar(() -> {
            verificar(fachada.ejecutarCompraRapida("cliente", servidor, List.of(TipoServicio.MANEJO_FRAGIL))
                    == EstadoCompra.RECHAZADA, "Rechazo por monto");
            verificar(fachada.ejecutarCompraRapida("BLOQUEADO", mouse, List.of())
                    == EstadoCompra.RECHAZADA, "Rechazo por usuario");
        });
        verificar(proveedor.creaciones == 4 && proveedor.cobros == 4,
                "Rechazos no crean ni invocan procesadores");
        verificar(rechazos.contains("$6.025.000,00 COP"), "Manejo fragil antes de validar monto");
        verificar(!rechazos.contains("AUDITORÍA"), "Rechazos sin auditoria aprobada");

        proveedor.fallar = true;
        String fallida = capturar(() -> verificar(fachada.ejecutarCompraRapida("cliente", mouse, List.of())
                == EstadoCompra.ERROR_PASARELA, "Distinguir fallo bancario de rechazo"));
        verificar(!fallida.contains("AUDITORÍA") && !fallida.contains("EXITOSA"),
                "No anunciar exito ante un fallo");
        verificar(proveedor.cobros == 5, "Sin reintentos ante fallos");

        String integrada = capturar(() -> verificar(new FachadaCompra("clave-simulacion")
                .ejecutarCompraRapida("cliente_vip", combo, List.of(TipoServicio.GARANTIA, TipoServicio.EMPAQUE))
                == EstadoCompra.EXITOSA, "Cadena completa con pasarela simulada"));
        verificar(integrada.contains("USD 289.75") && integrada.contains("StripeUSA (simulacion)"),
                "Fachada configura proxy, proveedor y adaptador");

        List<DefinicionProducto> componentes = new ArrayList<>();
        componentes.add(mouse);
        DefinicionProducto inmutable = DefinicionProducto.crearCombo("Combo", componentes);
        componentes.clear();
        verificar(inmutable.obtenerComponentes().size() == 1, "Copia defensiva de componentes");
        esperarError(UnsupportedOperationException.class, () -> inmutable.obtenerComponentes().clear());
        esperarError(IllegalStateException.class, () -> combo.obtenerPrecioIndividual());
        esperarError(IllegalArgumentException.class, () -> DefinicionProducto.crearIndividual("Mouse", -1));
        esperarError(NullPointerException.class, () -> fachada.ejecutarCompraRapida("cliente", null, List.of()));
        esperarError(NullPointerException.class, () -> fachada.ejecutarCompraRapida("cliente", mouse, null));
        List<TipoServicio> serviciosInvalidos = new ArrayList<>();
        serviciosInvalidos.add(null);
        esperarError(NullPointerException.class,
                () -> fachada.ejecutarCompraRapida("cliente", mouse, serviciosInvalidos));
        verificar(proveedor.cobros == 5, "Datos invalidos no solicitan pagos");

        System.out.print(integrada);
        System.out.print(rechazos);
        System.out.print(fallida);
        System.out.println("Pruebas de Facade superadas.");
    }

    private static class ProveedorRegistro implements ProveedorProcesador {
        private int creaciones;
        private int cobros;
        private double monto;
        private String usuario;
        private boolean fallar;

        @Override
        public ProcesadorPago crearProcesador() {
            creaciones++;
            return new ProcesadorPago() {
                @Override
                public void cobrarEnPesos(String cliente, double montoCOP) {
                    cobros++;
                    usuario = cliente;
                    monto = montoCOP;
                    if (fallar) {
                        throw new IllegalStateException("Fallo bancario simulado.");
                    }
                }
            };
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

    private static void esperarError(Class<? extends RuntimeException> tipo, Runnable operacion) {
        try {
            operacion.run();
        } catch (RuntimeException error) {
            verificar(tipo.isInstance(error), "Tipo de excepcion esperado");
            return;
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + descripcion);
        }
    }
}
