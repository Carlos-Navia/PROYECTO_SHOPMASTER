package shopmaster.compra;

import java.util.List;
import java.util.Objects;

/** Datos inmutables de entrada; la fachada construye el catalogo a partir de ellos. */
public final class DefinicionProducto {
    private final String nombre;
    private final Double precioIndividual;
    private final List<DefinicionProducto> componentes;

    private DefinicionProducto(String nombre, Double precioIndividual,
            List<DefinicionProducto> componentes) {
        Objects.requireNonNull(nombre, "El nombre es obligatorio.");
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
        this.nombre = nombre;
        this.precioIndividual = precioIndividual;
        this.componentes = List.copyOf(componentes);
    }

    public static DefinicionProducto crearIndividual(String nombre, double precio) {
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("El precio debe ser finito y no negativo.");
        }
        return new DefinicionProducto(nombre, precio, List.of());
    }

    public static DefinicionProducto crearCombo(String nombre, List<DefinicionProducto> componentes) {
        return new DefinicionProducto(nombre, null,
                Objects.requireNonNull(componentes, "Los componentes son obligatorios."));
    }

    public String obtenerNombre() {
        return nombre;
    }

    public boolean esCombo() {
        return precioIndividual == null;
    }

    public double obtenerPrecioIndividual() {
        if (esCombo()) {
            throw new IllegalStateException("Un combo no tiene precio individual.");
        }
        return precioIndividual;
    }

    public List<DefinicionProducto> obtenerComponentes() {
        return componentes;
    }
}
