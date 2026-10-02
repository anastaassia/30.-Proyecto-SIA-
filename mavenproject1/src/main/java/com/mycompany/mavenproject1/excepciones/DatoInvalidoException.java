package com.mycompany.mavenproject1.excepciones;

/**
 * Se lanza cuando un dato ingresado no cumple las reglas del negocio
 * (RUT mal formado, campo vacío, número fuera de rango, operación no permitida, etc.).
 */
public class DatoInvalidoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje explicación de por qué el dato es inválido
     */
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
