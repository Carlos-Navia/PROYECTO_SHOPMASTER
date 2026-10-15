package shopmaster.pagos;

import java.util.Objects;

/** Conserva configuracion; crea las dependencias bancarias solo bajo demanda. */
public class ProveedorStripe implements ProveedorProcesador {
    private final String claveComercio;

    public ProveedorStripe(String claveComercio) {
        Objects.requireNonNull(claveComercio, "La clave de comercio es obligatoria.");
        if (claveComercio.isBlank()) {
            throw new IllegalArgumentException("La clave de comercio no puede estar vacia.");
        }
        this.claveComercio = claveComercio;
    }

    @Override
    public ProcesadorPago crearProcesador() {
        return new AdaptadorStripe(new StripeUSA(), claveComercio);
    }
}
