package com.mycompany.mavenproject1.reportes;

import com.mycompany.mavenproject1.control.Control;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase padre de los reportes de seguimiento del sistema.
 * <p>
 * Define el formato común (título, descripción y líneas) y deja a cada subclase
 * la responsabilidad de decidir qué información incluir, sobrescribiendo
 * {@link #generarLineas(Control)}, {@link #getDescripcion()} y
 * {@link #mensajeSinResultados()}.
 */
public abstract class Reporte {

    private String titulo;

    /**
     * @param titulo título que se muestra en el encabezado del reporte
     */
    protected Reporte(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Explica qué muestra el reporte. Las subclases la sobrescriben.
     *
     * @return descripción del reporte
     */
    public String getDescripcion() {
        return "Reporte del sistema de intercambio.";
    }

    /**
     * Calcula el contenido del reporte. Cada subclase la sobrescribe con su propio criterio.
     *
     * @param control controlador con los datos del sistema
     * @return líneas de contenido (sin encabezado)
     */
    protected abstract List<String> generarLineas(Control control);

    /**
     * Texto que se muestra cuando el reporte no encuentra resultados.
     * Las subclases pueden sobrescribirlo con un mensaje más específico.
     *
     * @return mensaje para un reporte vacío
     */
    protected String mensajeSinResultados() {
        return "Sin resultados.";
    }

    /**
     * Genera el reporte completo: encabezado + contenido. Es igual para todos los
     * reportes; lo que cambia es lo que entrega cada subclase en {@link #generarLineas}.
     *
     * @param control controlador con los datos del sistema
     * @return líneas del reporte listas para mostrar
     */
    public final List<String> generar(Control control) {
        List<String> salida = new ArrayList<>();
        salida.add("=== " + titulo.toUpperCase() + " ===");
        salida.add(getDescripcion());
        salida.add("");
        List<String> contenido = generarLineas(control);
        if (contenido.isEmpty()) {
            salida.add(mensajeSinResultados());
        } else {
            salida.addAll(contenido);
        }
        return salida;
    }

    /**
     * @param control controlador con los datos del sistema
     * @return el reporte como un solo texto
     */
    public String generarTexto(Control control) {
        return String.join(System.lineSeparator(), generar(control));
    }

    @Override
    public String toString() {
        return titulo;
    }
}
