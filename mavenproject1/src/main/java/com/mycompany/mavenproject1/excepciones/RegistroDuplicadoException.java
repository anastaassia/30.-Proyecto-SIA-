package com.mycompany.mavenproject1.excepciones;

/**
 * Se lanza al intentar agregar un elemento cuya clave ya existe
 * (RUT de estudiante, ID de convenio o trámite activo repetido).
 */
public class RegistroDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje descripción del registro repetido
     */
    public RegistroDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
