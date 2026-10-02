package com.mycompany.mavenproject1.modelo;

import java.time.LocalDate;

/**
 * Documento que un estudiante subió a su trámite.
 */
public class DocumentoSubido {

    private TipoDocumento tipo;
    private String nombreArchivo;
    private LocalDate fechaSubida;

    /**
     * @param tipo          tipo de documento
     * @param nombreArchivo nombre del archivo entregado (ej. pasaporte.pdf)
     * @param fechaSubida   fecha en que se subió
     */
    public DocumentoSubido(TipoDocumento tipo, String nombreArchivo, LocalDate fechaSubida) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.fechaSubida = fechaSubida;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDocumento tipo) {
        this.tipo = tipo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public LocalDate getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDate fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    @Override
    public String toString() {
        return tipo.getEtiqueta() + ": " + nombreArchivo + " (" + fechaSubida + ")";
    }
}
