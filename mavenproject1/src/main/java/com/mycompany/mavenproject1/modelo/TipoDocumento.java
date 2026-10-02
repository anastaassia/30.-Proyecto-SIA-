package com.mycompany.mavenproject1.modelo;

/**
 * Tipos de documento que un convenio puede exigir como requisito.
 * En los archivos CSV se guarda el nombre de la constante (ej. PASAPORTE);
 * en pantalla se muestra la etiqueta legible.
 */
public enum TipoDocumento {
    CERT_NACIMIENTO("Certificado de nacimiento"),
    CERT_ALUMNO_REGULAR("Certificado de alumno regular"),
    PASAPORTE("Pasaporte"),
    CERTIFICADO_NOTAS("Certificado de notas"),
    CARTA_MOTIVACION("Carta de motivación"),
    CV("Currículum vitae"),
    CERTIFICADO_IDIOMA("Certificado de idioma");

    private final String etiqueta;

    TipoDocumento(String etiqueta) {
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
