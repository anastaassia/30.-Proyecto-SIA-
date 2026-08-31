package com.mycompany.mavenproject1;
import java.util.*;

public class Convenio
{
     //ATRIBUTOS
    private String idConvenio;
    private String nombre;
    private String universidadSocia;
    private String pais;
    private String duracion; //en meses
    private String carreraAsociada;
    //private String estado; //estado del convenio, si esta vigente para postular o no.
    
    //Requisitos para postular al convenio(documentos pedidos)
    private Set<TipoDocumento> requisitos = new HashSet<>();
    //Coleccion anidada Tramites
    private List<Tramite> tramites = new ArrayList<>();
    
    //CONSTRUCTOR
    public Convenio(String idConvenio, String nombre, String universidadSocia, String pais, Set<TipoDocumento> requi, String duracion, String carreraAsociada)//, String estado) 
    {
        this.idConvenio = idConvenio;
        this.nombre = nombre;
        this.universidadSocia = universidadSocia;
        this.pais = pais;
        this.duracion = duracion;
        this.carreraAsociada = carreraAsociada;
        //this.estado = estado;
        if (requi != null) this.requisitos.addAll(requi);
    }
    
    //SETTERS Y GETTERS
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

    public String getDuracion() {
        return duracion;
    }
    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getCarreraAsociada() {
        return carreraAsociada;
    }
    public void setCarreraAsociada(String carreraAsociada) {
        this.carreraAsociada = carreraAsociada;
    }

    //public String getEstado() {
    //    return estado;
    //}
    //public void setEstado(String estado) {
    //    this.estado = estado;
    //}
    
    /*public List<Tramite> getTramites(){
        return tramites; 
    }*/

    // No retorna coleccion asi
    public List<Tramite> getTramites() {
        return Collections.unmodifiableList(tramites);  // copia defensiva inmutable
    }

    //METODOS
    // Se crea un tramite para un estudiante especifico
    public Tramite crearTramite(Estudiante estudiante) 
    {
        Tramite t = new Tramite(java.util.UUID.randomUUID().toString(), estudiante);
        tramites.add(t);
        validarYActualizarEstado(t);
        return t;
    }
    
    // Aquí se hace la validación de los archivos, para ver si esta completo el tramite o no
    public void validarYActualizarEstado(Tramite t) 
    {
        if (t.estaCompleto(requisitos)) 
        {
            t.setEstado(Tramite.Estado.COMPLETO);
        } else 
        {
            t.setEstado(Tramite.Estado.EN_PROCESO);
        }
    }
    

    public Set<TipoDocumento> getRequisitos() {
        return Collections.unmodifiableSet(requisitos);
    }
    
    // Gestión de los requisitos para los convenios, pero por Menú
    public void setRequisitos(Set<TipoDocumento> requi) 
    {
        this.requisitos.clear();
        if (requi != null) 
            this.requisitos.addAll(requi);
        for (Tramite t : tramites) 
            validarYActualizarEstado(t);
    }
    
    public void agregarRequisito(TipoDocumento t) 
    {
        if (t != null) 
        {
            this.requisitos.add(t);
            for (Tramite trami : tramites) 
                validarYActualizarEstado(trami);
        }
    }
    public void quitarRequisito(TipoDocumento t) 
    {
        if (t != null) {
            this.requisitos.remove(t);
            for (Tramite trami : tramites) validarYActualizarEstado(trami);
        }
    }
    

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Convenio)) return false;
        Convenio other = (Convenio) o;
        return this.idConvenio != null && this.idConvenio.equals(other.idConvenio);
    }

    @Override
    public int hashCode() 
    {
        return idConvenio == null ? 0 : idConvenio.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Convenio{id=%s, nombre=%s, universidad=%s, país=%s}",
            idConvenio, nombre, universidadSocia, pais);
    }
}
