package shopmaster.pagos;

import java.util.Locale;
import java.util.Objects;
import shopmaster.excepciones.PagoRechazadoException;

/** Controla el acceso al procesador antes de permitir su creacion y uso. */
public class ProxyPagoSeguro implements ProcesadorPago {
    private static final double LIMITE_MONTO_COP = 5000000;
    private static final String USUARIO_BLOQUEADO = "BLOQUEADO";
    private final ProveedorProcesador proveedor;

    public ProxyPagoSeguro(ProveedorProcesador proveedor) {
        this.proveedor = Objects.requireNonNull(proveedor, "El proveedor es obligatorio.");
    }

    @Override
    public void cobrarEnPesos(String usuario, double montoCOP) {
        validarSolicitud(usuario, montoCOP);

        // Sin cache: cada solicitud aprobada crea su propio procesador.
        ProcesadorPago procesador = Objects.requireNonNull(proveedor.crearProcesador(),
                "El proveedor debe devolver un procesador.");
        procesador.cobrarEnPesos(usuario, montoCOP);

        // Un fallo del proveedor o de la pasarela se propaga sin auditar exito.
        System.out.printf(Locale.forLanguageTag("es-CO"),
                "[AUDITORÍA: Transacción aprobada para %s por $%,.2f COP]%n",
                usuario, montoCOP);
    }

    private void validarSolicitud(String usuario, double montoCOP) {
        if (usuario == null || usuario.isBlank()) {
            rechazar("El usuario es obligatorio y no puede estar vacio.");
        }
        if (!Double.isFinite(montoCOP) || montoCOP < 0) {
            rechazar("El monto debe ser finito y no negativo.");
        }
        if (montoCOP > LIMITE_MONTO_COP) {
            rechazar("El monto supera el limite de $5.000.000 COP.");
        }
        if (USUARIO_BLOQUEADO.equals(usuario)) {
            rechazar("El usuario BLOQUEADO esta reportado y no puede realizar compras.");
        }
    }

    private void rechazar(String motivo) {
        System.out.println("Pago rechazado: " + motivo);
        throw new PagoRechazadoException(motivo);
    }
}
