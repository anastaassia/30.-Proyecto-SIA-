package com.mycompany.mavenproject1.modelo;

/**
 * Estado general de un estudiante dentro del proceso de intercambio.
 */
public enum EstadoProceso {
    POSTULACION("Postulación"),
    ACEPTADO("Aceptado"),
    RECHAZADO("Rechazado"),
    EN_CURSO("En curso");

    private final String etiqueta;

    EstadoProceso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return nombre legible para mostrar al usuario */
    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
