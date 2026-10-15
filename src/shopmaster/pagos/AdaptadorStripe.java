package shopmaster.pagos;

import java.util.Locale;
import java.util.Objects;

/** Traduce el contrato local en COP al contrato externo de StripeUSA. */
public class AdaptadorStripe implements ProcesadorPago {
    private static final double TASA_CONVERSION = 4000;
    private final StripeUSA pasarela;
    private final String claveComercio;

    public AdaptadorStripe(StripeUSA pasarela, String claveComercio) {
        this.pasarela = Objects.requireNonNull(pasarela, "La pasarela es obligatoria.");
        Objects.requireNonNull(claveComercio, "La clave de comercio es obligatoria.");
        if (claveComercio.isBlank()) {
            throw new IllegalArgumentException("La clave de comercio no puede estar vacia.");
        }
        this.claveComercio = claveComercio;
    }

    @Override
    public void cobrarEnPesos(String usuario, double montoCOP) {
        Objects.requireNonNull(usuario, "El usuario es obligatorio.");
        if (usuario.isBlank()) {
            throw new IllegalArgumentException("El usuario no puede estar vacio.");
        }
        if (!Double.isFinite(montoCOP) || montoCOP < 0) {
            throw new IllegalArgumentException("El monto debe ser finito y no negativo.");
        }
        double montoUSD = convertirADolares(montoCOP);
        System.out.printf(Locale.forLanguageTag("es-CO"),
                "Conversion de $%,.2f COP a USD %s (1 USD = $%,.2f COP).%n",
                montoCOP, Double.toString(montoUSD), TASA_CONVERSION);
        // La simulacion recibe el identificador local; no se inventa un correo.
        // Los errores de la pasarela se propagan para que la fachada los gestione.
        pasarela.chargeInDollars(montoUSD, usuario, claveComercio);
    }

    private double convertirADolares(double montoCOP) {
        return montoCOP / TASA_CONVERSION;
    }
}
