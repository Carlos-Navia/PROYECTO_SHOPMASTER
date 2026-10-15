package shopmaster.catalogo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Compuesto que delega el calculo y el desglose a sus componentes. */
public class ComboProducto implements ElementoCatalogo {
    private final String nombre;
    private final List<ElementoCatalogo> componentes;

    public ComboProducto(String nombre) {
        Objects.requireNonNull(nombre, "El nombre es obligatorio.");
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
        this.nombre = nombre;
        this.componentes = new ArrayList<>();
    }

    /** Agrega un elemento sin permitir ciclos directos ni indirectos. */
    public void agregarComponente(ElementoCatalogo elemento) {
        Objects.requireNonNull(elemento, "El componente es obligatorio.");
        if (elemento == this) {
            throw new IllegalArgumentException("Un combo no puede contenerse a si mismo.");
        }
        // Esta comprobacion solo protege la estructura; el calculo es polimorfico.
        if (elemento instanceof ComboProducto) {
            ComboProducto combo = (ComboProducto) elemento;
            if (combo.contieneCombo(this)) {
                throw new IllegalArgumentException("El componente produciria un ciclo.");
            }
        }
        componentes.add(elemento);
    }

    /** Elimina una aparicion del componente; informa si estaba presente. */
    public boolean quitarComponente(ElementoCatalogo elemento) {
        Objects.requireNonNull(elemento, "El componente es obligatorio.");
        return componentes.remove(elemento);
    }

    private boolean contieneCombo(ComboProducto buscado) {
        if (this == buscado) {
            return true;
        }
        for (ElementoCatalogo componente : componentes) {
            if (componente instanceof ComboProducto) {
                ComboProducto combo = (ComboProducto) componente;
                if (combo.contieneCombo(buscado)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public double calcularPrecio() {
        double total = 0;
        for (ElementoCatalogo componente : componentes) {
            total += componente.calcularPrecio();
        }
        return total;
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public void mostrarDesglose(String sangria) {
        Objects.requireNonNull(sangria, "La sangria es obligatoria.");
        System.out.printf(Locale.forLanguageTag("es-CO"),
                "%s- %s (combo): $%,.2f COP%n", sangria, nombre, calcularPrecio());
        for (ElementoCatalogo componente : componentes) {
            componente.mostrarDesglose(sangria + "  ");
        }
    }
}
