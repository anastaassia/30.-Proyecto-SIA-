package com.mycompany.mavenproject1.excepciones;

/**
 * Se lanza cuando se busca u opera sobre un trámite cuyo ID no existe.
 */
public class TramiteNoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param idTramite ID del trámite que no se encontró
     */
    public TramiteNoEncontradoException(String idTramite) {
        super("No existe un trámite con ID " + idTramite + ".");
    }
}
