package com.mycompany.mavenproject1.control;

import com.mycompany.mavenproject1.excepciones.ConvenioNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import com.mycompany.mavenproject1.excepciones.DocumentoDuplicadoException;
import com.mycompany.mavenproject1.excepciones.EstudianteNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import com.mycompany.mavenproject1.excepciones.TramiteNoEncontradoException;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.EstadoProceso;
import com.mycompany.mavenproject1.modelo.EstadoTramite;
import com.mycompany.mavenproject1.modelo.Estudiante;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import com.mycompany.mavenproject1.modelo.Tramite;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador del sistema (la "C" de MVC). Contiene las colecciones principales
 * y toda la lógica de negocio; la consola y las ventanas solo llaman a sus métodos.
 * <ul>
 *   <li>Colección 1: {@code convenios} (List), cada convenio con su colección anidada de trámites.</li>
 *   <li>Mapa: {@code estudiantes} (HashMap con el RUT como clave).</li>
 * </ul>
 * Los errores se informan con excepciones propias, que las interfaces capturan con try-catch.
 */
public class Control {

    private List<Convenio> convenios = new ArrayList<>();
    private Map<String, Estudiante> estudiantes = new HashMap<>();

    // ============================================================ getters / setters

    /** @return vista de solo lectura de los convenios */
    public List<Convenio> getConvenios() {
        return Collections.unmodifiableList(convenios);
    }

    /** @param convenios nueva lista de convenios (se guarda una copia) */
    public void setConvenios(List<Convenio> convenios) {
        this.convenios = new ArrayList<>(convenios);
    }

    /** @return vista de solo lectura del mapa RUT → estudiante */
    public Map<String, Estudiante> getEstudiantes() {
        return Collections.unmodifiableMap(estudiantes);
    }

    /** @param estudiantes nuevo mapa de estudiantes (se guarda una copia) */
    public void setEstudiantes(Map<String, Estudiante> estudiantes) {
        this.estudiantes = new HashMap<>(estudiantes);
    }

    // ============================================================ ESTUDIANTES

    /**
     * Registra un estudiante nuevo validando todos sus datos.
     *
     * @param estudiante estudiante a registrar
     * @throws DatoInvalidoException      si algún dato no es válido
     * @throws RegistroDuplicadoException si ya existe un estudiante con ese RUT
     */
    public void registrarEstudiante(Estudiante estudiante)
            throws DatoInvalidoException, RegistroDuplicadoException {
        String rut = Validador.normalizarRut(estudiante.getRut());
        if (estudiantes.containsKey(rut)) {
            throw new RegistroDuplicadoException("Ya existe un estudiante con RUT " + rut + ".");
        }
        estudiante.setRut(rut);
        estudiante.setNombre(Validador.textoObligatorio(estudiante.getNombre(), "Nombre"));
        estudiante.setCarrera(Validador.textoObligatorio(estudiante.getCarrera(), "Carrera"));
        Validador.anioIngreso(estudiante.getAnioIngreso());
        if (estudiante.getEstadoProceso() == null) {
            estudiante.setEstadoProceso(EstadoProceso.POSTULACION);
        }
        estudiantes.put(rut, estudiante);
    }

    /**
     * @param rut RUT con o sin puntos
     * @return el estudiante
     * @throws EstudianteNoEncontradoException si no existe
     */
    public Estudiante buscarEstudiante(String rut) throws EstudianteNoEncontradoException {
        Estudiante e = estudiantes.get(normalizarSiEsPosible(rut));
        if (e == null) {
            throw new EstudianteNoEncontradoException(rut);
        }
        return e;
    }

    /** @return estudiantes ordenados por nombre */
    public List<Estudiante> listarEstudiantes() {
        List<Estudiante> lista = new ArrayList<>(estudiantes.values());
        lista.sort(Comparator.comparing(Estudiante::getNombre, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    /**
     * @param texto parte del RUT, nombre o carrera
     * @return estudiantes que coinciden, ordenados por nombre
     */
    public List<Estudiante> buscarEstudiantes(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        List<Estudiante> resultado = new ArrayList<>();
        for (Estudiante e : listarEstudiantes()) {
            String datos = (e.getRut() + " " + e.getNombre() + " " + e.getCarrera()).toLowerCase();
            if (datos.contains(q)) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    /**
     * Modifica los datos de un estudiante (el RUT no se puede cambiar porque es la clave).
     *
     * @param rut         RUT del estudiante
     * @param nombre      nuevo nombre
     * @param carrera     nueva carrera
     * @param anioIngreso nuevo año de ingreso
     * @param estado      nuevo estado del proceso
     * @throws EstudianteNoEncontradoException si no existe
     * @throws DatoInvalidoException           si algún dato no es válido
     */
    public void editarEstudiante(String rut, String nombre, String carrera, int anioIngreso, EstadoProceso estado)
            throws EstudianteNoEncontradoException, DatoInvalidoException {
        Estudiante e = buscarEstudiante(rut);
        String nombreValido = Validador.textoObligatorio(nombre, "Nombre");
        String carreraValida = Validador.textoObligatorio(carrera, "Carrera");
        Validador.anioIngreso(anioIngreso);
        if (estado == null) {
            throw new DatoInvalidoException("Debe indicar el estado del proceso.");
        }
        e.setNombre(nombreValido);
        e.setCarrera(carreraValida);
        e.setAnioIngreso(anioIngreso);
        e.setEstadoProceso(estado);
    }

    /**
     * Elimina un estudiante y todos sus trámites (eliminación en cascada).
     *
     * @param rut RUT del estudiante
     * @return cantidad de trámites eliminados junto con él
     * @throws EstudianteNoEncontradoException si no existe
     */
    public int eliminarEstudiante(String rut) throws EstudianteNoEncontradoException {
        Estudiante e = buscarEstudiante(rut);
        int tramitesEliminados = 0;
        for (Convenio c : convenios) {
            tramitesEliminados += c.eliminarTramitesDeEstudiante(e.getRut());
        }
        estudiantes.remove(e.getRut());
        return tramitesEliminados;
    }

    // ============================================================ CONVENIOS

    /**
     * Agrega un convenio nuevo validando sus datos.
     *
     * @param convenio convenio a agregar
     * @throws DatoInvalidoException      si algún dato no es válido
     * @throws RegistroDuplicadoException si ya existe un convenio con ese ID
     */
    public void agregarConvenio(Convenio convenio) throws DatoInvalidoException, RegistroDuplicadoException {
        String id = Validador.textoObligatorio(convenio.getIdConvenio(), "ID del convenio").toUpperCase();
        if (id.contains(" ")) {
            throw new DatoInvalidoException("El ID del convenio no puede tener espacios.");
        }
        if (existeConvenio(id)) {
            throw new RegistroDuplicadoException("Ya existe un convenio con ID " + id + ".");
        }
        convenio.setIdConvenio(id);
        convenio.setNombre(Validador.textoObligatorio(convenio.getNombre(), "Nombre"));
        convenio.setUniversidadSocia(Validador.textoObligatorio(convenio.getUniversidadSocia(), "Universidad socia"));
        convenio.setPais(Validador.textoObligatorio(convenio.getPais(), "País"));
        convenio.setCarreraAsociada(Validador.textoObligatorio(convenio.getCarreraAsociada(), "Carrera asociada"));
        Validador.duracionMeses(convenio.getDuracionMeses());
        if (convenio.getRequisitos().isEmpty()) {
            throw new DatoInvalidoException("El convenio debe exigir al menos un documento.");
        }
        convenios.add(convenio);
    }

    /**
     * @param idConvenio ID del convenio (no distingue mayúsculas)
     * @return el convenio
     * @throws ConvenioNoEncontradoException si no existe
     */
    public Convenio buscarConvenio(String idConvenio) throws ConvenioNoEncontradoException {
        for (Convenio c : convenios) {
            if (c.getIdConvenio().equalsIgnoreCase(idConvenio == null ? "" : idConvenio.trim())) {
                return c;
            }
        }
        throw new ConvenioNoEncontradoException(idConvenio);
    }

    private boolean existeConvenio(String idConvenio) {
        try {
            buscarConvenio(idConvenio);
            return true;
        } catch (ConvenioNoEncontradoException e) {
            return false;
        }
    }

    /** @return copia de la lista de convenios */
    public List<Convenio> listarConvenios() {
        return new ArrayList<>(convenios);
    }

    /**
     * @param texto parte del ID, nombre, universidad, país o carrera
     * @return convenios que coinciden
     */
    public List<Convenio> buscarConvenios(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        List<Convenio> resultado = new ArrayList<>();
        for (Convenio c : convenios) {
            String datos = (c.getIdConvenio() + " " + c.getNombre() + " " + c.getUniversidadSocia() + " "
                    + c.getPais() + " " + c.getCarreraAsociada()).toLowerCase();
            if (datos.contains(q)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * Modifica los datos de un convenio (el ID no se puede cambiar). Si cambian los
     * requisitos, el convenio recalcula el estado de sus trámites.
     *
     * @param idConvenio       ID del convenio
     * @param nombre           nuevo nombre
     * @param universidadSocia nueva universidad
     * @param pais             nuevo país
     * @param duracionMeses    nueva duración
     * @param carreraAsociada  nueva carrera
     * @param requisitos       nuevos documentos exigidos
     * @throws ConvenioNoEncontradoException si no existe
     * @throws DatoInvalidoException         si algún dato no es válido
     */
    public void editarConvenio(String idConvenio, String nombre, String universidadSocia, String pais,
                               int duracionMeses, String carreraAsociada, Set<TipoDocumento> requisitos)
            throws ConvenioNoEncontradoException, DatoInvalidoException {
        Convenio c = buscarConvenio(idConvenio);
        String nombreValido = Validador.textoObligatorio(nombre, "Nombre");
        String uniValida = Validador.textoObligatorio(universidadSocia, "Universidad socia");
        String paisValido = Validador.textoObligatorio(pais, "País");
        String carreraValida = Validador.textoObligatorio(carreraAsociada, "Carrera asociada");
        Validador.duracionMeses(duracionMeses);
        if (requisitos == null || requisitos.isEmpty()) {
            throw new DatoInvalidoException("El convenio debe exigir al menos un documento.");
        }
        c.setNombre(nombreValido);
        c.setUniversidadSocia(uniValida);
        c.setPais(paisValido);
        c.setDuracionMeses(duracionMeses);
        c.setCarreraAsociada(carreraValida);
        c.setRequisitos(requisitos);
    }

    /**
     * Elimina un convenio junto con sus trámites.
     *
     * @param idConvenio ID del convenio
     * @return cantidad de trámites eliminados junto con él
     * @throws ConvenioNoEncontradoException si no existe
     */
    public int eliminarConvenio(String idConvenio) throws ConvenioNoEncontradoException {
        Convenio c = buscarConvenio(idConvenio);
        List<Tramite> tramites = new ArrayList<>(c.getTramites());
        convenios.remove(c);
        for (Tramite t : tramites) {
            sincronizarEstadoEstudiante(t.getEstudiante());
        }
        return tramites.size();
    }

    // ============================================================ TRÁMITES

    /**
     * Crea un trámite para un estudiante en un convenio. El ID se genera solo (T-0001, T-0002...).
     *
     * @param idConvenio ID del convenio
     * @param rut        RUT del estudiante
     * @return el trámite creado
     * @throws ConvenioNoEncontradoException   si el convenio no existe
     * @throws EstudianteNoEncontradoException si el estudiante no existe
     * @throws RegistroDuplicadoException      si el estudiante ya tiene un trámite activo en ese convenio
     */
    public Tramite crearTramite(String idConvenio, String rut)
            throws ConvenioNoEncontradoException, EstudianteNoEncontradoException, RegistroDuplicadoException {
        Convenio c = buscarConvenio(idConvenio);
        Estudiante e = buscarEstudiante(rut);
        Tramite t = new Tramite(generarIdTramite(), e, c);
        c.agregarTramite(t);
        sincronizarEstadoEstudiante(e);
        return t;
    }

    /**
     * Agrega un trámite ya construido (lo usa la persistencia al cargar tramites.csv).
     *
     * @param idConvenio convenio al que pertenece
     * @param tramite    trámite a agregar
     * @throws ConvenioNoEncontradoException si el convenio no existe
     * @throws RegistroDuplicadoException    si el ID ya existe en el sistema
     */
    public void agregarTramite(String idConvenio, Tramite tramite)
            throws ConvenioNoEncontradoException, RegistroDuplicadoException {
        Convenio c = buscarConvenio(idConvenio);
        for (Convenio otro : convenios) {
            if (otro.buscarTramite(tramite.getIdTramite()) != null) {
                throw new RegistroDuplicadoException("Ya existe un trámite con ID " + tramite.getIdTramite() + ".");
            }
        }
        c.agregarTramite(tramite);
    }

    /**
     * @param idTramite ID del trámite (no distingue mayúsculas)
     * @return el trámite
     * @throws TramiteNoEncontradoException si no existe en ningún convenio
     */
    public Tramite buscarTramite(String idTramite) throws TramiteNoEncontradoException {
        String id = idTramite == null ? "" : idTramite.trim();
        for (Convenio c : convenios) {
            Tramite t = c.buscarTramite(id);
            if (t != null) {
                return t;
            }
        }
        throw new TramiteNoEncontradoException(idTramite);
    }

    /**
     * Sobrecarga 1: todos los trámites del sistema.
     *
     * @return todos los trámites, ordenados por ID
     */
    public List<Tramite> listarTramites() {
        List<Tramite> lista = new ArrayList<>();
        for (Convenio c : convenios) {
            lista.addAll(c.getTramites());
        }
        lista.sort(Comparator.comparing(Tramite::getIdTramite));
        return lista;
    }

    /**
     * Sobrecarga 2: trámites de un convenio.
     *
     * @param idConvenio ID del convenio
     * @return trámites del convenio, ordenados por ID
     * @throws ConvenioNoEncontradoException si el convenio no existe
     */
    public List<Tramite> listarTramites(String idConvenio) throws ConvenioNoEncontradoException {
        List<Tramite> lista = new ArrayList<>(buscarConvenio(idConvenio).getTramites());
        lista.sort(Comparator.comparing(Tramite::getIdTramite));
        return lista;
    }

    /**
     * Sobrecarga 3: trámites en un estado.
     *
     * @param estado estado buscado
     * @return trámites en ese estado, ordenados por ID
     */
    public List<Tramite> listarTramites(EstadoTramite estado) {
        List<Tramite> lista = new ArrayList<>();
        for (Tramite t : listarTramites()) {
            if (t.getEstado() == estado) {
                lista.add(t);
            }
        }
        return lista;
    }

    /**
     * @param texto parte del ID, RUT, nombre del estudiante o ID del convenio
     * @return trámites que coinciden
     */
    public List<Tramite> buscarTramites(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        List<Tramite> resultado = new ArrayList<>();
        for (Tramite t : listarTramites()) {
            String datos = (t.getIdTramite() + " " + t.getEstudiante().getRut() + " "
                    + t.getEstudiante().getNombre() + " " + t.getConvenio().getIdConvenio()).toLowerCase();
            if (datos.contains(q)) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    /**
     * Cambia el estado de un trámite (aprobar, rechazar o reabrir) y su observación.
     * Además actualiza el estado del estudiante.
     *
     * @param idTramite   ID del trámite
     * @param nuevoEstado estado solicitado
     * @param observacion comentario de la coordinación (puede ser vacío)
     * @throws TramiteNoEncontradoException si no existe
     * @throws DatoInvalidoException        si el cambio de estado no está permitido
     */
    public void editarTramite(String idTramite, EstadoTramite nuevoEstado, String observacion)
            throws TramiteNoEncontradoException, DatoInvalidoException {
        Tramite t = buscarTramite(idTramite);
        String obs = Validador.textoOpcional(observacion, "Observación");
        if (nuevoEstado == null) {
            throw new DatoInvalidoException("Debe indicar el nuevo estado.");
        }
        t.cambiarEstado(nuevoEstado);
        t.setObservacion(obs);
        sincronizarEstadoEstudiante(t.getEstudiante());
    }

    /**
     * @param idTramite ID del trámite a eliminar
     * @throws TramiteNoEncontradoException si no existe
     */
    public void eliminarTramite(String idTramite) throws TramiteNoEncontradoException {
        Tramite t = buscarTramite(idTramite);
        t.getConvenio().eliminarTramite(t.getIdTramite());
        sincronizarEstadoEstudiante(t.getEstudiante());
    }

    /**
     * Sube un documento a un trámite.
     *
     * @param idTramite     ID del trámite
     * @param tipo          tipo de documento
     * @param nombreArchivo nombre del archivo
     * @param reemplazar    true para reemplazar un documento del mismo tipo ya subido
     * @throws TramiteNoEncontradoException si el trámite no existe
     * @throws DocumentoDuplicadoException  si ya hay un documento de ese tipo y reemplazar es false
     * @throws DatoInvalidoException        si el documento no se exige o el trámite ya está resuelto
     */
    public void subirDocumento(String idTramite, TipoDocumento tipo, String nombreArchivo, boolean reemplazar)
            throws TramiteNoEncontradoException, DocumentoDuplicadoException, DatoInvalidoException {
        Tramite t = buscarTramite(idTramite);
        String archivo = Validador.textoObligatorio(nombreArchivo, "Nombre del archivo");
        if (tipo == null) {
            throw new DatoInvalidoException("Debe indicar el tipo de documento.");
        }
        if (t.tieneDocumento(tipo) && !reemplazar) {
            throw new DocumentoDuplicadoException("El trámite " + t.getIdTramite() + " ya tiene el documento \""
                    + tipo.getEtiqueta() + "\" (" + t.getDocumentos().get(tipo).getNombreArchivo() + ").");
        }
        t.subirDocumento(tipo, archivo);
    }

    /**
     * @param idTramite ID del trámite
     * @param tipo      tipo de documento a quitar
     * @throws TramiteNoEncontradoException si el trámite no existe
     * @throws DatoInvalidoException        si el documento no estaba subido o el trámite ya está resuelto
     */
    public void quitarDocumento(String idTramite, TipoDocumento tipo)
            throws TramiteNoEncontradoException, DatoInvalidoException {
        Tramite t = buscarTramite(idTramite);
        if (!t.quitarDocumento(tipo)) {
            throw new DatoInvalidoException("El trámite " + t.getIdTramite() + " no tiene el documento \""
                    + tipo.getEtiqueta() + "\".");
        }
    }

    /**
     * Mantiene coherente el estado del estudiante con sus trámites:
     * con un trámite aprobado queda ACEPTADO; si todos fueron rechazados, RECHAZADO;
     * en otro caso, POSTULACION. El estado EN_CURSO lo asigna la coordinación y no se toca.
     *
     * @param e estudiante a revisar
     */
    private void sincronizarEstadoEstudiante(Estudiante e) {
        if (e.getEstadoProceso() == EstadoProceso.EN_CURSO) {
            return;
        }
        boolean tieneTramites = false;
        boolean todosRechazados = true;
        for (Tramite t : listarTramites()) {
            if (t.getEstudiante().equals(e)) {
                tieneTramites = true;
                if (t.getEstado() == EstadoTramite.APROBADO) {
                    e.setEstadoProceso(EstadoProceso.ACEPTADO);
                    return;
                }
                if (t.getEstado() != EstadoTramite.RECHAZADO) {
                    todosRechazados = false;
                }
            }
        }
        e.setEstadoProceso(tieneTramites && todosRechazados ? EstadoProceso.RECHAZADO : EstadoProceso.POSTULACION);
    }

    /** @return siguiente ID libre con formato T-0001 */
    private String generarIdTramite() {
        int mayor = 0;
        for (Tramite t : listarTramites()) {
            String id = t.getIdTramite();
            if (id.matches("T-\\d+")) {
                mayor = Math.max(mayor, Integer.parseInt(id.substring(2)));
            }
        }
        return String.format("T-%04d", mayor + 1);
    }

    private String normalizarSiEsPosible(String rut) {
        try {
            return Validador.normalizarRut(rut);
        } catch (DatoInvalidoException e) {
            return rut == null ? "" : rut.trim();
        }
    }

    // ============================================================ DATOS INICIALES

    /** @return true si no hay convenios ni estudiantes */
    public boolean estaVacio() {
        return convenios.isEmpty() && estudiantes.isEmpty();
    }

    /** Borra todos los datos en memoria. */
    public void limpiar() {
        convenios.clear();
        estudiantes.clear();
    }

    /**
     * Carga datos de ejemplo que permiten probar todas las funcionalidades:
     * 5 convenios (uno sin trámites), 8 estudiantes (uno sin trámites) y 7 trámites
     * en todos los estados posibles, con documentos subidos y faltantes.
     *
     * @throws DatoInvalidoException      no debería ocurrir con estos datos
     * @throws RegistroDuplicadoException no debería ocurrir con estos datos
     */
    public void cargarDatosIniciales() throws DatoInvalidoException, RegistroDuplicadoException {
        limpiar();
        agregarConvenio(new Convenio("ES-SAL-2026", "Intercambio Salamanca", "Universidad de Salamanca", "España",
                6, "Ingeniería Civil Informática", EnumSet.of(TipoDocumento.PASAPORTE,
                TipoDocumento.CERTIFICADO_NOTAS, TipoDocumento.CARTA_MOTIVACION)));
        agregarConvenio(new Convenio("DE-TUM-2026", "Doble titulación TUM", "Technische Universität München",
                "Alemania", 12, "Ingeniería Civil Informática", EnumSet.of(TipoDocumento.PASAPORTE,
                TipoDocumento.CERTIFICADO_NOTAS, TipoDocumento.CERTIFICADO_IDIOMA, TipoDocumento.CV)));
        agregarConvenio(new Convenio("MX-UNAM-2026", "Semestre en la UNAM", "Universidad Nacional Autónoma de México",
                "México", 6, "Arquitectura", EnumSet.of(TipoDocumento.CERT_ALUMNO_REGULAR,
                TipoDocumento.CERTIFICADO_NOTAS, TipoDocumento.CARTA_MOTIVACION)));
        agregarConvenio(new Convenio("FR-LYON-2026", "Intercambio Lyon", "Université Lumière Lyon 2", "Francia",
                6, "Ingeniería Comercial", EnumSet.of(TipoDocumento.PASAPORTE,
                TipoDocumento.CERT_ALUMNO_REGULAR, TipoDocumento.CERTIFICADO_IDIOMA)));
        agregarConvenio(new Convenio("AR-UBA-2026", "Pasantía Buenos Aires", "Universidad de Buenos Aires",
                "Argentina", 3, "Psicología", EnumSet.of(TipoDocumento.CERT_ALUMNO_REGULAR,
                TipoDocumento.CERT_NACIMIENTO)));

        registrarEstudiante(new Estudiante("20.123.456-5", "Camila Rojas Soto", "Ingeniería Civil Informática", 2022));
        registrarEstudiante(new Estudiante("20.987.654-K", "Matías González Pérez", "Ingeniería Civil Informática", 2023));
        registrarEstudiante(new Estudiante("19.876.543-0", "Valentina Muñoz Araya", "Arquitectura", 2021));
        registrarEstudiante(new Estudiante("21.234.567-9", "Benjamín Silva Torres", "Ingeniería Civil Informática", 2023));
        registrarEstudiante(new Estudiante("20.555.444-0", "Catalina Fuentes Lagos", "Ingeniería Civil Informática", 2022));
        registrarEstudiante(new Estudiante("19.444.333-1", "Diego Morales Vera", "Ingeniería Comercial", 2021));
        registrarEstudiante(new Estudiante("21.111.222-0", "Francisca Contreras Díaz", "Ingeniería Comercial", 2024));
        registrarEstudiante(new Estudiante("20.333.222-K", "Tomás Herrera Pino", "Psicología", 2023));

        LocalDate agosto = LocalDate.of(2026, 8, 10);
        try {
            Tramite t1 = crearTramite("ES-SAL-2026", "20.123.456-5");
            t1.subirDocumento(TipoDocumento.PASAPORTE, "pasaporte_crojas.pdf", agosto);
            t1.subirDocumento(TipoDocumento.CERTIFICADO_NOTAS, "notas_crojas.pdf", agosto);
            t1.subirDocumento(TipoDocumento.CARTA_MOTIVACION, "carta_crojas.pdf", agosto.plusDays(2));
            editarTramite(t1.getIdTramite(), EstadoTramite.APROBADO, "Cumple todos los requisitos");

            Tramite t2 = crearTramite("ES-SAL-2026", "20.987.654-K");
            t2.subirDocumento(TipoDocumento.PASAPORTE, "pasaporte_mgonzalez.pdf", agosto.plusDays(5));
            t2.subirDocumento(TipoDocumento.CERTIFICADO_NOTAS, "notas_mgonzalez.pdf", agosto.plusDays(5));

            Tramite t3 = crearTramite("DE-TUM-2026", "21.234.567-9");
            t3.subirDocumento(TipoDocumento.PASAPORTE, "pasaporte_bsilva.pdf", agosto.plusDays(1));
            t3.subirDocumento(TipoDocumento.CERTIFICADO_NOTAS, "notas_bsilva.pdf", agosto.plusDays(1));

            Tramite t4 = crearTramite("DE-TUM-2026", "20.555.444-0");
            t4.subirDocumento(TipoDocumento.PASAPORTE, "pasaporte_cfuentes.pdf", agosto.plusDays(3));
            t4.subirDocumento(TipoDocumento.CERTIFICADO_NOTAS, "notas_cfuentes.pdf", agosto.plusDays(3));
            t4.subirDocumento(TipoDocumento.CERTIFICADO_IDIOMA, "goethe_b2_cfuentes.pdf", agosto.plusDays(4));
            t4.subirDocumento(TipoDocumento.CV, "cv_cfuentes.pdf", agosto.plusDays(4));

            Tramite t5 = crearTramite("MX-UNAM-2026", "19.876.543-0");
            t5.subirDocumento(TipoDocumento.CERTIFICADO_NOTAS, "notas_vmunoz.pdf", agosto.plusDays(7));

            Tramite t6 = crearTramite("FR-LYON-2026", "19.444.333-1");
            t6.subirDocumento(TipoDocumento.PASAPORTE, "pasaporte_dmorales.pdf", agosto);
            t6.subirDocumento(TipoDocumento.CERT_ALUMNO_REGULAR, "alumno_regular_dmorales.pdf", agosto);
            t6.subirDocumento(TipoDocumento.CERTIFICADO_IDIOMA, "delf_b1_dmorales.pdf", agosto);
            editarTramite(t6.getIdTramite(), EstadoTramite.RECHAZADO, "Nivel de idioma inferior al B2 exigido");

            crearTramite("FR-LYON-2026", "21.111.222-0");
        } catch (ConvenioNoEncontradoException | EstudianteNoEncontradoException | TramiteNoEncontradoException e) {
            throw new DatoInvalidoException("Error en los datos iniciales: " + e.getMessage());
        }
    }
}
