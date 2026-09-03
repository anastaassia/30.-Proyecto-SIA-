package com.mycompany.mavenproject1;
public class EstudianteNoEncontradoException extends Exception 
{
    public EstudianteNoEncontradoException(String rut) {
        super("No se encontró estudiante con RUT: " + rut);
    }
}
