package com.mycompany.mavenproject1.modelo;

import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Trámite de postulación de un estudiante a un convenio.
 * Guarda los documentos subidos en un mapa con clave {@link TipoDocumento},
 * por lo que hay a lo más un documento de cada tipo.
 */
public class Tramite {

    private String idTramite;
    private Estudiante estudiante;
    private Convenio convenio;
    private Map<TipoDocumento, DocumentoSubido> documentos = new EnumMap<>(TipoDocumento.class);
    private EstadoTramite estado = EstadoTramite.EN_PROCESO;
    private String observacion = "";

    /**
     * @param idTramite  identificador único (ej. T-0001)
     * @param estudiante estudiante que postula
     * @param convenio   convenio al que postula
     */
    public Tramite(String idTramite, Estudiante estudiante, Convenio convenio) {
        this.idTramite = idTramite;
        this.estudiante = estudiante;
        this.convenio = convenio;
    }

    // ------------------------------------------------------------ getters / setters

    public String getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(String idTramite) {
        this.idTramite = idTramite;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    /**
     * Retorna una vista de solo lectura. Para modificar los documentos se usan
     * {@link #subirDocumento(TipoDocumento, String)} y {@link #quitarDocumento(TipoDocumento)}.
     *
     * @return documentos subidos, sin posibilidad de modificarlos desde fuera
     */
    public Map<TipoDocumento, DocumentoSubido> getDocumentos() {
        return Collections.unmodifiableMap(documentos);
    }

    /**
     * Reemplaza todos los documentos por una copia del mapa recibido.
     *
     * @param documentos nuevos documentos
     */
    public void setDocumentos(Map<TipoDocumento, DocumentoSubido> documentos) {
        this.documentos = new EnumMap<>(TipoDocumento.class);
        if (documentos != null) {
            this.documentos.putAll(documentos);
        }
        actualizarEstado();
    }

    public EstadoTramite getEstado() {
        return estado;
    }

    public void setEstado(EstadoTramite estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion == null ? "" : observacion;
    }

    // ------------------------------------------------------------ documentos (sobrecarga)

    /**
     * Sube un documento con la fecha de hoy. Es la versión que usan las interfaces
     * cuando el usuario sube un documento.
     *
     * @param tipo          tipo de documento
     * @param nombreArchivo nombre del archivo
     * @throws DatoInvalidoException si el trámite ya fue resuelto o el convenio no exige ese documento
     */
    public void subirDocumento(TipoDocumento tipo, String nombreArchivo) throws DatoInvalidoException {
        subirDocumento(tipo, nombreArchivo, LocalDate.now());
    }

    /**
     * Sube un documento con una fecha específica. La usa la persistencia para
     * conservar la fecha original guardada en documentos.csv, y los datos iniciales.
     *
     * @param tipo          tipo de documento
     * @param nombreArchivo nombre del archivo
     * @param fechaSubida   fecha en que se subió el documento
     * @throws DatoInvalidoException si el trámite ya fue resuelto o el convenio no exige ese documento
     */
    public void subirDocumento(TipoDocumento tipo, String nombreArchivo, LocalDate fechaSubida)
            throws DatoInvalidoException {
        if (estado.esResuelto()) {
            throw new DatoInvalidoException("El trámite " + idTramite + " ya está "
                    + estado.getEtiqueta().toLowerCase() + "; no se pueden modificar sus documentos.");
        }
        if (convenio != null && !convenio.getRequisitos().contains(tipo)) {
            throw new DatoInvalidoException("El convenio " + convenio.getIdConvenio()
                    + " no exige el documento \"" + tipo.getEtiqueta() + "\".");
        }
        documentos.put(tipo, new DocumentoSubido(tipo, nombreArchivo, fechaSubida));
        actualizarEstado();
    }

    /**
     * Quita un documento del trámite.
     *
     * @param tipo tipo de documento a quitar
     * @return true si el documento existía y se quitó
     * @throws DatoInvalidoException si el trámite ya fue resuelto
     */
    public boolean quitarDocumento(TipoDocumento tipo) throws DatoInvalidoException {
        if (estado.esResuelto()) {
            throw new DatoInvalidoException("El trámite " + idTramite + " ya está "
                    + estado.getEtiqueta().toLowerCase() + "; no se pueden modificar sus documentos.");
        }
        boolean existia = documentos.remove(tipo) != null;
        actualizarEstado();
        return existia;
    }

    /**
     * @param tipo tipo de documento
     * @return true si ya se subió un documento de ese tipo
     */
    public boolean tieneDocumento(TipoDocumento tipo) {
        return documentos.containsKey(tipo);
    }

    // ------------------------------------------------------------ estado

    /**
     * @return documentos que exige el convenio y que aún no se suben
     */
    public Set<TipoDocumento> getDocumentosFaltantes() {
        Set<TipoDocumento> faltantes = EnumSet.noneOf(TipoDocumento.class);
        if (convenio != null) {
            for (TipoDocumento requisito : convenio.getRequisitos()) {
                if (!documentos.containsKey(requisito)) {
                    faltantes.add(requisito);
                }
            }
        }
        return faltantes;
    }

    /** @return true si se subieron todos los documentos que exige el convenio */
    public boolean estaCompleto() {
        return getDocumentosFaltantes().isEmpty();
    }

    /**
     * Recalcula el estado según los documentos (EN_PROCESO o COMPLETO).
     * No modifica un trámite que la coordinación ya aprobó o rechazó.
     */
    public void actualizarEstado() {
        if (!estado.esResuelto()) {
            estado = estaCompleto() ? EstadoTramite.COMPLETO : EstadoTramite.EN_PROCESO;
        }
    }

    /**
     * Cambia el estado del trámite respetando las reglas del negocio:
     * <ul>
     *   <li>APROBADO solo si el trámite está completo.</li>
     *   <li>RECHAZADO en cualquier momento.</li>
     *   <li>EN_PROCESO / COMPLETO reabren el trámite, y deben coincidir con los documentos subidos.</li>
     * </ul>
     *
     * @param nuevoEstado estado solicitado
     * @throws DatoInvalidoException si el cambio no cumple las reglas
     */
    public void cambiarEstado(EstadoTramite nuevoEstado) throws DatoInvalidoException {
        if (nuevoEstado == EstadoTramite.APROBADO && !estaCompleto()) {
            throw new DatoInvalidoException("No se puede aprobar el trámite " + idTramite
                    + ": faltan documentos " + getDocumentosFaltantes() + ".");
        }
        if (nuevoEstado.esResuelto()) {
            estado = nuevoEstado;
            return;
        }
        EstadoTramite calculado = estaCompleto() ? EstadoTramite.COMPLETO : EstadoTramite.EN_PROCESO;
        if (nuevoEstado != calculado) {
            throw new DatoInvalidoException("El estado \"" + nuevoEstado.getEtiqueta()
                    + "\" se calcula según los documentos: este trámite quedaría \""
                    + calculado.getEtiqueta() + "\".");
        }
        estado = calculado;
    }

    /** Dos trámites son el mismo si tienen el mismo ID. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tramite)) {
            return false;
        }
        return idTramite != null && idTramite.equals(((Tramite) o).idTramite);
    }

    @Override
    public int hashCode() {
        return idTramite == null ? 0 : idTramite.hashCode();
    }

    @Override
    public String toString() {
        return idTramite + " - " + (estudiante == null ? "?" : estudiante.getNombre())
                + " (" + estado.getEtiqueta() + ")";
    }
}
