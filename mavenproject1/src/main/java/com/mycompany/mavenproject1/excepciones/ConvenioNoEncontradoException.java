package com.mycompany.mavenproject1.excepciones;

/**
 * Se lanza cuando se busca u opera sobre un convenio cuyo ID no existe.
 */
public class ConvenioNoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param idConvenio ID del convenio que no se encontró
     */
    public ConvenioNoEncontradoException(String idConvenio) {
        super("No existe un convenio con ID " + idConvenio + ".");
    }
}
