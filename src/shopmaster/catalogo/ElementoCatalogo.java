package shopmaster.catalogo;

/**
 * Contrato comun para productos individuales y combos del catalogo.
 * Permite utilizar ambos mediante el patron Composite.
 */
public interface ElementoCatalogo {

    /**
     * Obtiene el precio en COP; un combo suma los precios de sus componentes.
     *
     * @return precio total en pesos colombianos
     */
    double calcularPrecio();

    /**
     * @return nombre del producto o combo
     */
    String obtenerNombre();

    /**
     * Muestra el elemento y sus componentes de forma recursiva.
     *
     * @param sangria espacios que representan el nivel actual del arbol
     */
    void mostrarDesglose(String sangria);
}
