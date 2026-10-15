package shopmaster.pagos;

import java.util.Locale;

/**
 * Simulacion academica de la pasarela externa; no realiza cobros reales.
 * Su firma representa el contrato del proveedor y debe conservarse.
 */
public class StripeUSA {

    /**
     * Simula el cobro en USD. En este ejercicio customerEmail recibe el
     * identificador local del usuario, sin afirmar que sea un correo validado.
     * La clave comercial no se imprime en consola.
     */
    public void chargeInDollars(double amountUSD, String customerEmail, String merchantKey) {
        System.out.printf(Locale.forLanguageTag("es-CO"),
                "StripeUSA (simulacion): cobro de USD %s para %s.%n",
                Double.toString(amountUSD), customerEmail);
    }
}
