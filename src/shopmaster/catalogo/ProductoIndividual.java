package shopmaster.catalogo;

import java.util.Locale;
import java.util.Objects;

/** Hoja del patron Composite: representa un producto con precio propio. */
public class ProductoIndividual implements ElementoCatalogo {
    private final String nombre;
    private final double precio;

    public ProductoIndividual(String nombre, double precio) {
        Objects.requireNonNull(nombre, "El nombre es obligatorio.");
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("El precio debe ser finito y no negativo.");
        }
        this.nombre = nombre;
        this.precio = precio;
    }

    @Override
    public double calcularPrecio() {
        return precio;
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public void mostrarDesglose(String sangria) {
        Objects.requireNonNull(sangria, "La sangria es obligatoria.");
        System.out.printf(Locale.forLanguageTag("es-CO"),
                "%s- %s: $%,.2f COP%n", sangria, nombre, precio);
    }
}
