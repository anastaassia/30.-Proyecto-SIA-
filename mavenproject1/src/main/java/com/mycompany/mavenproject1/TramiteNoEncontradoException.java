package com.mycompany.mavenproject1;
public class TramiteNoEncontradoException extends Exception {
    public TramiteNoEncontradoException(String idTramite) {
        super("No se encontró el trámite con ID: " + idTramite);
    }
}

