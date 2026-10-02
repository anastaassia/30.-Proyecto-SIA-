package com.mycompany.mavenproject1.modelo;

/**
 * Estado de un trámite de postulación.
 * <ul>
 *   <li>EN_PROCESO y COMPLETO se calculan automáticamente según los documentos subidos.</li>
 *   <li>APROBADO y RECHAZADO los asigna la coordinación al resolver el trámite.</li>
 * </ul>
 */
public enum EstadoTramite {
    EN_PROCESO("En proceso"),
    COMPLETO("Completo"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String etiqueta;

    EstadoTramite(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return nombre legible para mostrar al usuario */
    public String getEtiqueta() {
        return etiqueta;
    }

    /** @return true si el estado fue asignado por la coordinación (aprobado o rechazado) */
    public boolean esResuelto() {
        return this == APROBADO || this == RECHAZADO;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
