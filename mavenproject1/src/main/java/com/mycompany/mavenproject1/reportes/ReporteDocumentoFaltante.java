package com.mycompany.mavenproject1.reportes;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import com.mycompany.mavenproject1.modelo.Tramite;
import java.util.ArrayList;
import java.util.List;

/**
 * Funcionalidad propia (SIA-9): seguimiento de documentos faltantes.
 * <p>
 * Filtra los trámites que todavía están abiertos y a los que les falta un tipo de
 * documento específico, opcionalmente dentro de un solo convenio. Sirve para que la
 * coordinación sepa a qué estudiantes contactar (ej. "¿a quién le falta el pasaporte?").
 */
public class ReporteDocumentoFaltante extends Reporte {

    private TipoDocumento tipoDocumento;
    private String idConvenio;

    /**
     * @param tipoDocumento documento que se quiere revisar
     * @param idConvenio    ID del convenio a revisar, o null para revisar todos
     */
    public ReporteDocumentoFaltante(TipoDocumento tipoDocumento, String idConvenio) {
        super("Documentos faltantes");
        this.tipoDocumento = tipoDocumento;
        this.idConvenio = idConvenio;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getIdConvenio() {
        return idConvenio;
    }

    public void setIdConvenio(String idConvenio) {
        this.idConvenio = idConvenio;
    }

    @Override
    public String getDescripcion() {
        return "Trámites abiertos a los que les falta: " + tipoDocumento.getEtiqueta()
                + (idConvenio == null ? " (todos los convenios)" : " (convenio " + idConvenio + ")");
    }

    @Override
    protected List<String> generarLineas(Control control) {
        List<String> lineas = new ArrayList<>();
        for (Convenio c : control.listarConvenios()) {
            boolean convenioIncluido = idConvenio == null || c.getIdConvenio().equalsIgnoreCase(idConvenio);
            if (!convenioIncluido || !c.getRequisitos().contains(tipoDocumento)) {
                continue;
            }
            for (Tramite t : c.getTramites()) {
                if (!t.getEstado().esResuelto() && !t.tieneDocumento(tipoDocumento)) {
                    lineas.add(String.format("%s | %s | %-24s | %s | faltan %d/%d",
                            t.getIdTramite(), t.getEstudiante().getRut(), t.getEstudiante().getNombre(),
                            c.getIdConvenio(), t.getDocumentosFaltantes().size(), c.getRequisitos().size()));
                }
            }
        }
        if (!lineas.isEmpty()) {
            lineas.add("");
            int total = lineas.size() - 1;
            lineas.add("Total: " + total + " trámite(s) pendientes de este documento.");
        }
        return lineas;
    }

    @Override
    protected String mensajeSinResultados() {
        return "Ningún trámite abierto tiene pendiente este documento.";
    }
}
