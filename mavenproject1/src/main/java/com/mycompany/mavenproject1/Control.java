package com.mycompany.mavenproject1;
import java.util.*;

public class Control
{
    //Primera coleccion, convenios
    private List<Convenio> convenios = new ArrayList<>();
    // Mapa de estudiantes por rut
    //private Map<String, Estudiante> estudiantes = new HashMap<>();
    private HashMap<String, Estudiante> estudiantes = new HashMap<>();
    
    //GETTER Y SETTER
    public List<Convenio> getConvenios() 
    {
        return List.copyOf(convenios);
    }
    
    public Collection<Estudiante> getEstudiantes() 
    {
    return Collections.unmodifiableCollection(new ArrayList<>(estudiantes.values()));
    }

    // MÉTODOS y sobrecarga de metodos
    public void registrarEstudiante(Estudiante e) 
    {  
        estudiantes.put(e.getRut(), e);
    }
    public void registrarEstudiante(String rut, String nombre, String carrera, int anioIngreso) 
    {
        Estudiante e = new Estudiante(rut, nombre, carrera, anioIngreso, "Postulación", null);
        registrarEstudiante(e);
    }
    
    public void agregarConvenio(Convenio c)
    { 
        convenios.add(c); 
    }

    public Estudiante buscarEstudiante(String rut) 
    { 
        return estudiantes.get(rut); 
    }
    
    public Convenio buscarConvenio(String codigoConvenio) 
    {
        for (Convenio c : convenios)
        {
            if (c.getIdConvenio().equalsIgnoreCase(codigoConvenio))
            { 
                return c;
            }
        }
        return null;
    }
    
    public List<Estudiante> listarEstudiantes() 
    {
        List<Estudiante> lista = new ArrayList<>(estudiantes.values()); // copia defensiva
        lista.sort(Comparator.comparing(Estudiante::getNombre, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(lista); // no se puede modficar desde fuera
    }
    
    // Buscar un trámite por id dentro de un convenio
    public Tramite buscarTramite(String idConvenio, String idTramite) {
        Convenio c = buscarConvenio(idConvenio);
        if (c == null) return null;
        for (Tramite t : c.getTramites()) {
            if (t.getIdTramite().equals(idTramite)) return t;
        }
        return null;
    }

    // Crear trámite para un estudiante en un convenio (idTramite opcional)
    public boolean crearTramite(String idConvenio, String rutEstudiante, String idTramiteOpcional) {
        Convenio c = buscarConvenio(idConvenio);
        Estudiante e = buscarEstudiante(rutEstudiante);
        if (c == null || e == null) return false;

        Tramite t = c.crearTramite(e); 
        if (idTramiteOpcional != null && !idTramiteOpcional.isBlank()) {
            t.setIdTramite(idTramiteOpcional);
        }
        // recalculamos estado por si hay requisitos
        c.validarYActualizarEstado(t);
        return true;
    }

    //Listar trámites de un convenio
    public java.util.List<Tramite> listarTramites(String idConvenio) {
        Convenio c = buscarConvenio(idConvenio);
        return (c == null) ? java.util.Collections.emptyList() : new java.util.ArrayList<>(c.getTramites());
    }

    //Editar (estado y/o estudiante asignado)
    public boolean editarTramite(String idConvenio, String idTramite,
                                 Tramite.Estado nuevoEstado, String nuevoRutEstudiante) {
        Convenio c = buscarConvenio(idConvenio);
        if (c == null) return false;
        Tramite t = buscarTramite(idConvenio, idTramite);
        if (t == null) return false;

        if (nuevoRutEstudiante != null && !nuevoRutEstudiante.isBlank()) {
            Estudiante nuevo = buscarEstudiante(nuevoRutEstudiante);
            if (nuevo == null) return false;
            t.setEstudiante(nuevo);
        }
        if (nuevoEstado != null) {
            t.setEstado(nuevoEstado);
        }
        c.validarYActualizarEstado(t);
        return true;
    }

    // Eliminar trámite
    public boolean eliminarTramite(String idConvenio, String idTramite) {
        Convenio c = buscarConvenio(idConvenio);
        if (c == null) return false;
        return c.getTramites().removeIf(x -> x.getIdTramite().equals(idTramite));
    }

    // Acciones de documentos dentro del trámite
    public boolean subirDocumentoATramite(String idConvenio, String idTramite,
                                          TipoDocumento tipo, String nombreArchivo) {
        Convenio c = buscarConvenio(idConvenio);
        if (c == null) return false;
        Tramite t = buscarTramite(idConvenio, idTramite);
        if (t == null) return false;

        t.subirDocumento(tipo, nombreArchivo);
        c.validarYActualizarEstado(t);
        return true;
    }

    public boolean eliminarDocumentoDeTramite(String idConvenio, String idTramite, TipoDocumento tipo) {
        Convenio c = buscarConvenio(idConvenio);
        if (c == null) return false;
        Tramite t = buscarTramite(idConvenio, idTramite);
        if (t == null) return false;

        if (t.eliminarDocumento(tipo)) {
            c.validarYActualizarEstado(t);
            return true;
        }
        return false;
    }

    
    
    //Agregar datos iniciales
    public void datos() {
        // Estudiantes
        registrarEstudiante("11.111.111-1", "Ana Pérez", "ICI", 2022);
        registrarEstudiante("22.222.222-2", "Bruno Díaz", "ICINF", 2021);

        // Convenios y sus requisitos
        Set<TipoDocumento> reqA = new HashSet<>(Arrays.asList(
            TipoDocumento.CERT_NACIMIENTO, TipoDocumento.CERT_ALUMNO_REGULAR
        ));
        Set<TipoDocumento> reqB = new HashSet<>(Arrays.asList(
            TipoDocumento.PASAPORTE, TipoDocumento.CERTIFICADO_NOTAS
        ));

        agregarConvenio(new Convenio("A-2026", "Convenio A", "Universidad A", "País A", reqA, "Tres meses", "Arquitectura"));
        agregarConvenio(new Convenio("B-2026", "Convenio B", "Universidad B", "País B", reqB, "Doce meses", "Ingeniería Industrial"));

        // Agregar un trámite de ejemplo para demostrar la sobrecarga con String
        Convenio convenioEjemplo = buscarConvenio("A-2026");
        Estudiante estudianteEjemplo = buscarEstudiante("11.111.111-1");
        if (convenioEjemplo != null && estudianteEjemplo != null) {
            Tramite tramiteEjemplo = convenioEjemplo.crearTramite(estudianteEjemplo);
            // Usamos la sobrecarga que recibe String (no el Enum)
            tramiteEjemplo.subirDocumento("CERT_NACIMIENTO", "cert_nacimiento_ejemplo.pdf");
            System.out.println("Documento subido usando sobrecarga String (demostración)");
        }
        
    }
    
    //  Excepciones, Try catch
    public Estudiante buscarEstudianteStrict(String rut) throws EstudianteNoEncontradoException {
        Estudiante e = buscarEstudiante(rut);
        if (e == null) throw new EstudianteNoEncontradoException(rut);
        return e;
    }

    public void subirDocumentoATramiteStrict(String idConvenio, String idTramite,
                                             TipoDocumento tipo, String nombreArchivo)
            throws TramiteNoEncontradoException, DocumentoDuplicadoException {
        Convenio c = buscarConvenio(idConvenio);
        if (c == null) throw new TramiteNoEncontradoException(idTramite); // Podemos crear ConvenioNoEncontradoException

        Tramite t = buscarTramite(idConvenio, idTramite);
        if (t == null) throw new TramiteNoEncontradoException(idTramite);

        t.subirDocumentoSeguro(tipo, nombreArchivo); // Aqui puede saltar DocumentoDuplicadoException
        c.validarYActualizarEstado(t);
    }
    
    // Eliminar y editar 
    public boolean editarConvenio(String id,
                                  String nuevoNombre,
                                  String nuevaUniversidad,
                                  String nuevoPais,
                                  String nuevaDuracion,
                                  String nuevaCarrera) {
        Convenio c = buscarConvenio(id);
        if (c == null) return false;
        if (nuevoNombre != null && !nuevoNombre.isBlank())      c.setNombre(nuevoNombre);
        if (nuevaUniversidad != null && !nuevaUniversidad.isBlank()) c.setUniversidadSocia(nuevaUniversidad);
        if (nuevoPais != null && !nuevoPais.isBlank())           c.setPais(nuevoPais);
        if (nuevaDuracion != null && !nuevaDuracion.isBlank())   c.setDuracion(nuevaDuracion);
        if (nuevaCarrera != null && !nuevaCarrera.isBlank())     c.setCarreraAsociada(nuevaCarrera);
        return true;
    }

    public boolean eliminarConvenio(String id) {
        // sacamos a los estudiantes que lo tengan
        for (Estudiante e : getEstudiantes()) {
            if (e.getConvenio() != null && e.getConvenio().getIdConvenio().equals(id)) {
                e.setConvenio(null);
            }
        }
        // eliminar de la lista
        return convenios.removeIf(x -> x.getIdConvenio().equals(id));
    }
    
    // Buscar en 1 o más niveles

    // --- Nivel 1: Estudiantes ---
    public java.util.List<Estudiante> buscarEstudiantesPorNombre(String q) 
    {
        String s = (q == null ? "" : q).toLowerCase();
        java.util.List<Estudiante> out = new java.util.ArrayList<>();
        for (Estudiante e : getEstudiantes()) {
            String hay = (e.getRut() + " " + e.getNombre() + " " + e.getCarrera() + " " + e.getEstadoProceso());
            if (hay.toLowerCase().contains(s)) out.add(e);
        }
        return out;
    }

    // --- Nivel 1: Convenios ---
    public java.util.List<Convenio> buscarConveniosPorId(String q) {
        String s = (q == null ? "" : q).toLowerCase();
        java.util.List<Convenio> out = new java.util.ArrayList<>();
        for (Convenio c : getConvenios()) {
            String reqs = "";
            if (!c.getRequisitos().isEmpty()) {
                StringBuilder b = new StringBuilder();
                for (TipoDocumento td : c.getRequisitos()) {
                    if (b.length() > 0) b.append(' ');
                    b.append(td.name());
                }
                reqs = b.toString();
            }
            String hay = (c.getIdConvenio() + " " + c.getNombre() + " " + c.getUniversidadSocia() + " " +
                          c.getPais() + " " + c.getDuracion() + " " + c.getCarreraAsociada() + " " + reqs);
            if (hay.toLowerCase().contains(s)) out.add(c);
        }
        return out;
    }

    //  Nivel 2: Trámites -> busca por idTramite, rut/nombre estudiante, estado, convenio, documentos
    public java.util.List<Tramite> buscarTramitesPorTexto(String q) {
        String s = (q == null ? "" : q).toLowerCase();
        java.util.List<Tramite> out = new java.util.ArrayList<>();
        for (Convenio c : getConvenios()) {
            for (Tramite t : c.getTramites()) {
                // aqui armamos una "bolsa" de texto 
                StringBuilder bag = new StringBuilder();
                bag.append(t.getIdTramite()).append(' ');
                bag.append(t.getEstado().name()).append(' ');
                if (t.getEstudiante() != null) {
                    bag.append(t.getEstudiante().getRut()).append(' ');
                    bag.append(t.getEstudiante().getNombre()).append(' ');
                }
                // agregar contexto del convenio
                bag.append(c.getIdConvenio()).append(' ')
                   .append(c.getNombre()).append(' ')
                   .append(c.getPais()).append(' ');

                // agregar documentos -> tipo y el nombre de archivo
                if (t.getDocumentos() != null && !t.getDocumentos().isEmpty()) {
                    for (java.util.Map.Entry<TipoDocumento, DocumentoSubido> e : t.getDocumentos().entrySet()) {
                        bag.append(e.getKey().name()).append(' ');
                        if (e.getValue() != null && e.getValue().getNombreArchivo() != null) {
                            bag.append(e.getValue().getNombreArchivo()).append(' ');
                        }
                    }
                }
                if (bag.toString().toLowerCase().contains(s)) out.add(t);
            }
        }
        return out;
    }

    //  Búsqueda específica por documentos (nombre de archivo o tipo) 
    public java.util.List<Tramite> buscarTramitesPorDocumento(String q) 
    {
        String s = (q == null ? "" : q).toLowerCase();
        java.util.List<Tramite> out = new java.util.ArrayList<>();
        for (Convenio c : getConvenios()) {
            for (Tramite t : c.getTramites()) {
                if (t.getDocumentos() == null || t.getDocumentos().isEmpty()) continue;
                boolean match = false;
                for (java.util.Map.Entry<TipoDocumento, DocumentoSubido> e : t.getDocumentos().entrySet()) {
                    if (e.getKey().name().toLowerCase().contains(s)) { match = true; break; }
                    DocumentoSubido d = e.getValue();
                    if (d != null && d.getNombreArchivo() != null && d.getNombreArchivo().toLowerCase().contains(s)) {
                        match = true; break;
                    }
                }
                if (match) out.add(t);
            }
        }
        return out;
    }

    //  Búsqueda Global (devuelve líneas formateadas con el tipo) 
    public java.util.List<String> buscarGlobal(String q) {
        java.util.List<String> res = new java.util.ArrayList<>();

        for (Estudiante e : buscarEstudiantesPorNombre(q)) {
            res.add("[ESTUDIANTE] " + e.getRut() + " | " + e.getNombre() + " | " + e.getCarrera() +
                    (e.getEstadoProceso() == null ? "" : " | Estado: " + e.getEstadoProceso()));
        }
        for (Convenio c : buscarConveniosPorId(q)) {
            res.add("[CONVENIO] " + c.getIdConvenio() + " | " + c.getNombre() + " | " + c.getUniversidadSocia() +
                    " | " + c.getPais());
        }
        for (Tramite t : buscarTramitesPorTexto(q)) {
            String rut = (t.getEstudiante() == null ? "-" : t.getEstudiante().getRut());
            res.add("[TRAMITE] " + t.getIdTramite() + " | Est: " + rut + " | Estado: " + t.getEstado());
        }
        return res;
    }

    public List<Estudiante> filtrarEstudiantesPorEstado(String estado) {
        return getEstudiantes().stream()
                .filter(e -> e.getEstadoProceso() != null && e.getEstadoProceso().equalsIgnoreCase(estado))
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean editarEstudiante(String rut, String nuevoNombre, 
                                 String nuevaCarrera, Integer nuevoAnio, 
                                 String nuevoEstado) {
    Estudiante e = buscarEstudiante(rut);
    if (e == null) return false;
    
    if (nuevoNombre != null && !nuevoNombre.isBlank()) {
        e.setNombre(nuevoNombre);
    }
    if (nuevaCarrera != null && !nuevaCarrera.isBlank()) {
        e.setCarrera(nuevaCarrera);
    }
    if (nuevoAnio != null && nuevoAnio > 0) {
        e.setAnioIngreso(nuevoAnio);
    }
    if (nuevoEstado != null && !nuevoEstado.isBlank()) {
        e.setEstadoProceso(nuevoEstado);
    }
    return true;
}

    /**
     * Elimina un estudiante del sistema
     * También lo elimina de todos los trámites asociados
     * @param rut RUT del estudiante a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean eliminarEstudiante(String rut) {
        Estudiante e = buscarEstudiante(rut);
        if (e == null) return false;

        // Eliminar de todos los trámites
        for (Convenio c : getConvenios()) {
            c.getTramites().removeIf(t -> 
                t.getEstudiante() != null && 
                t.getEstudiante().getRut().equals(rut)
            );
        }

        // Eliminar del mapa
        return estudiantes.remove(rut) != null;
    }
}

