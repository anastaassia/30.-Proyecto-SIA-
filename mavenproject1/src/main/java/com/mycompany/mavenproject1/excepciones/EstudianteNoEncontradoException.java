package com.mycompany.mavenproject1.excepciones;

/**
 * Se lanza cuando se busca u opera sobre un RUT que no está registrado en el sistema.
 */
public class EstudianteNoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param rut RUT que no se encontró
     */
    public EstudianteNoEncontradoException(String rut) {
        super("No existe un estudiante con RUT " + rut + ".");
    }
}
