package com.mycompany.mavenproject1.modelo;

import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Convenio de intercambio con una universidad socia.
 * Contiene la segunda colección del sistema (anidada): la lista de trámites
 * de los estudiantes que postulan a este convenio.
 */
public class Convenio {

    private String idConvenio;
    private String nombre;
    private String universidadSocia;
    private String pais;
    private int duracionMeses;
    private String carreraAsociada;
    private Set<TipoDocumento> requisitos = EnumSet.noneOf(TipoDocumento.class);
    private List<Tramite> tramites = new ArrayList<>();

    /**
     * @param idConvenio       identificador único (ej. ES-SAL-2026)
     * @param nombre           nombre del convenio
     * @param universidadSocia universidad de destino
     * @param pais             país de destino
     * @param duracionMeses    duración del intercambio en meses
     * @param carreraAsociada  carrera a la que está dirigido
     * @param requisitos       documentos que se exigen para postular
     */
    public Convenio(String idConvenio, String nombre, String universidadSocia, String pais,
                    int duracionMeses, String carreraAsociada, Set<TipoDocumento> requisitos) {
        this.idConvenio = idConvenio;
        this.nombre = nombre;
        this.universidadSocia = universidadSocia;
        this.pais = pais;
        this.duracionMeses = duracionMeses;
        this.carreraAsociada = carreraAsociada;
        if (requisitos != null) {
            this.requisitos.addAll(requisitos);
        }
    }

    // ------------------------------------------------------------ getters / setters

    public String getIdConvenio() {
        return idConvenio;
    }

    public void setIdConvenio(String idConvenio) {
        this.idConvenio = idConvenio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUniversidadSocia() {
        return universidadSocia;
    }

    public void setUniversidadSocia(String universidadSocia) {
        this.universidadSocia = universidadSocia;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public int getDuracionMeses() {
        return duracionMeses;
    }

    public void setDuracionMeses(int duracionMeses) {
        this.duracionMeses = duracionMeses;
    }

    public String getCarreraAsociada() {
        return carreraAsociada;
    }

    public void setCarreraAsociada(String carreraAsociada) {
        this.carreraAsociada = carreraAsociada;
    }

    /**
     * @return vista de solo lectura de los requisitos; se modifican con
     *         {@link #setRequisitos(Set)}, {@link #agregarRequisito} o {@link #quitarRequisito}
     */
    public Set<TipoDocumento> getRequisitos() {
        return Collections.unmodifiableSet(requisitos);
    }

    /**
     * Reemplaza los requisitos y recalcula el estado de todos los trámites.
     *
     * @param requisitos nuevos documentos exigidos
     */
    public void setRequisitos(Set<TipoDocumento> requisitos) {
        this.requisitos = EnumSet.noneOf(TipoDocumento.class);
        if (requisitos != null) {
            this.requisitos.addAll(requisitos);
        }
        actualizarEstadoTramites();
    }

    /**
     * @return vista de solo lectura de los trámites; se modifican con
     *         {@link #agregarTramite(Tramite)} y {@link #eliminarTramite(String)}
     */
    public List<Tramite> getTramites() {
        return Collections.unmodifiableList(tramites);
    }

    /**
     * Reemplaza la lista de trámites por una copia de la recibida.
     *
     * @param tramites nuevos trámites
     */
    public void setTramites(List<Tramite> tramites) {
        this.tramites = new ArrayList<>();
        if (tramites != null) {
            for (Tramite t : tramites) {
                t.setConvenio(this);
                this.tramites.add(t);
            }
        }
        actualizarEstadoTramites();
    }

    // ------------------------------------------------------------ requisitos

    /**
     * @param tipo documento que se pasa a exigir
     */
    public void agregarRequisito(TipoDocumento tipo) {
        requisitos.add(tipo);
        actualizarEstadoTramites();
    }

    /**
     * @param tipo documento que se deja de exigir
     */
    public void quitarRequisito(TipoDocumento tipo) {
        requisitos.remove(tipo);
        actualizarEstadoTramites();
    }

    /** Recalcula el estado de cada trámite cuando cambian los requisitos. */
    private void actualizarEstadoTramites() {
        for (Tramite t : tramites) {
            t.actualizarEstado();
        }
    }

    // ------------------------------------------------------------ trámites (colección anidada)

    /**
     * Agrega un trámite al convenio. Un estudiante no puede tener dos trámites
     * activos (no rechazados) en el mismo convenio.
     *
     * @param tramite trámite a agregar
     * @throws RegistroDuplicadoException si el ID ya existe o el estudiante ya tiene un trámite activo
     */
    public void agregarTramite(Tramite tramite) throws RegistroDuplicadoException {
        if (buscarTramite(tramite.getIdTramite()) != null) {
            throw new RegistroDuplicadoException("Ya existe el trámite " + tramite.getIdTramite()
                    + " en el convenio " + idConvenio + ".");
        }
        if (tieneTramiteActivo(tramite.getEstudiante().getRut())) {
            throw new RegistroDuplicadoException("El estudiante " + tramite.getEstudiante().getRut()
                    + " ya tiene un trámite activo en el convenio " + idConvenio + ".");
        }
        tramite.setConvenio(this);
        tramites.add(tramite);
        tramite.actualizarEstado();
    }

    /**
     * @param idTramite ID del trámite
     * @return el trámite, o null si no está en este convenio
     */
    public Tramite buscarTramite(String idTramite) {
        for (Tramite t : tramites) {
            if (t.getIdTramite().equalsIgnoreCase(idTramite)) {
                return t;
            }
        }
        return null;
    }

    /**
     * @param idTramite ID del trámite a eliminar
     * @return true si se eliminó
     */
    public boolean eliminarTramite(String idTramite) {
        Tramite t = buscarTramite(idTramite);
        return t != null && tramites.remove(t);
    }

    /**
     * Elimina todos los trámites de un estudiante (se usa al eliminar al estudiante).
     *
     * @param rut RUT del estudiante
     * @return cantidad de trámites eliminados
     */
    public int eliminarTramitesDeEstudiante(String rut) {
        int eliminados = 0;
        Iterator<Tramite> it = tramites.iterator();
        while (it.hasNext()) {
            if (it.next().getEstudiante().getRut().equals(rut)) {
                it.remove();
                eliminados++;
            }
        }
        return eliminados;
    }

    /**
     * @param rut RUT del estudiante
     * @return true si el estudiante tiene en este convenio un trámite que no fue rechazado
     */
    public boolean tieneTramiteActivo(String rut) {
        for (Tramite t : tramites) {
            if (t.getEstudiante().getRut().equals(rut) && t.getEstado() != EstadoTramite.RECHAZADO) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param estado estado a contar
     * @return cantidad de trámites del convenio en ese estado
     */
    public int contarTramites(EstadoTramite estado) {
        int total = 0;
        for (Tramite t : tramites) {
            if (t.getEstado() == estado) {
                total++;
            }
        }
        return total;
    }

    /** Dos convenios son el mismo si tienen el mismo ID. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Convenio)) {
            return false;
        }
        return idConvenio != null && idConvenio.equals(((Convenio) o).idConvenio);
    }

    @Override
    public int hashCode() {
        return idConvenio == null ? 0 : idConvenio.hashCode();
    }

    @Override
    public String toString() {
        return idConvenio + " - " + nombre;
    }
}
