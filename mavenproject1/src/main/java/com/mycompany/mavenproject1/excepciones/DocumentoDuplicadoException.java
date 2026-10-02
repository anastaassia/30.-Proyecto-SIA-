package com.mycompany.mavenproject1.excepciones;

/**
 * Se lanza al subir a un trámite un tipo de documento que ya fue subido.
 * La interfaz la captura para preguntar al usuario si desea reemplazarlo.
 */
public class DocumentoDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción del documento repetido
     */
    public DocumentoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
