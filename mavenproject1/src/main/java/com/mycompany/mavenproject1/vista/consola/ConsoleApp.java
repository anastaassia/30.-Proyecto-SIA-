package com.mycompany.mavenproject1.vista.consola;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.control.Validador;
import com.mycompany.mavenproject1.excepciones.ConvenioNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import com.mycompany.mavenproject1.excepciones.DocumentoDuplicadoException;
import com.mycompany.mavenproject1.excepciones.EstudianteNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import com.mycompany.mavenproject1.excepciones.TramiteNoEncontradoException;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.DocumentoSubido;
import com.mycompany.mavenproject1.modelo.EstadoProceso;
import com.mycompany.mavenproject1.modelo.EstadoTramite;
import com.mycompany.mavenproject1.modelo.Estudiante;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import com.mycompany.mavenproject1.modelo.Tramite;
import com.mycompany.mavenproject1.persistencia.DataStore;
import com.mycompany.mavenproject1.persistencia.ExportadorExcel;
import com.mycompany.mavenproject1.reportes.Reporte;
import com.mycompany.mavenproject1.reportes.ReporteAvanceConvenios;
import com.mycompany.mavenproject1.reportes.ReporteDocumentoFaltante;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Interfaz de consola. Ofrece exactamente las mismas funcionalidades que la interfaz
 * de ventanas, organizadas en un menú principal con un submenú por cada área.
 * Toda la lógica está en {@link Control}; esta clase solo pide datos y muestra resultados.
 */
public class ConsoleApp {

    private Control control;
    private DataStore dataStore;
    private Scanner entrada;

    /**
     * @param control   controlador con los datos ya cargados
     * @param dataStore persistencia, para guardar al salir
     */
    public ConsoleApp(Control control, DataStore dataStore) {
        this.control = control;
        this.dataStore = dataStore;
        this.entrada = new Scanner(System.in, "UTF-8");
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /** Muestra el menú principal hasta que el usuario elige salir; al salir guarda los datos. */
    public void iniciar() {
        System.out.println("============================================================");
        System.out.println("   SISTEMA DE GESTIÓN DE INTERCAMBIO ESTUDIANTIL (CONSOLA)");
        System.out.println("============================================================");
        mostrarAdvertenciasDeCarga();
        try {
            boolean salir = false;
            while (!salir) {
                System.out.println();
                System.out.println("========== MENÚ PRINCIPAL ==========");
                System.out.println(" 1. Estudiantes");
                System.out.println(" 2. Convenios");
                System.out.println(" 3. Trámites y documentos");
                System.out.println(" 4. Reportes de seguimiento");
                System.out.println(" 5. Exportar planilla Excel");
                System.out.println(" 6. Guardar datos ahora");
                System.out.println(" 7. Restaurar datos de ejemplo");
                System.out.println(" 0. Guardar y salir");
                switch (leerOpcion(7)) {
                    case 1: menuEstudiantes(); break;
                    case 2: menuConvenios(); break;
                    case 3: menuTramites(); break;
                    case 4: menuReportes(); break;
                    case 5: exportarExcel(); break;
                    case 6: guardar(); break;
                    case 7: restaurarDatosEjemplo(); break;
                    default: salir = true;
                }
            }
        } catch (EntradaCerradaException e) {
            System.out.println("\nSe cerró la entrada estándar.");
        }
        guardar();
        System.out.println("Hasta luego.");
    }

    // ================================================================ ESTUDIANTES

    private void menuEstudiantes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n----- ESTUDIANTES -----");
            System.out.println(" 1. Agregar estudiante");
            System.out.println(" 2. Listar estudiantes");
            System.out.println(" 3. Editar estudiante");
            System.out.println(" 4. Eliminar estudiante");
            System.out.println(" 5. Buscar estudiante");
            System.out.println(" 0. Volver");
            switch (leerOpcion(5)) {
                case 1: agregarEstudiante(); break;
                case 2: mostrarEstudiantes(control.listarEstudiantes()); break;
                case 3: editarEstudiante(); break;
                case 4: eliminarEstudiante(); break;
                case 5: mostrarEstudiantes(control.buscarEstudiantes(leerTexto("Texto a buscar (RUT, nombre o carrera)"))); break;
                default: volver = true;
            }
        }
    }

    private void agregarEstudiante() {
        System.out.println("\n--- Agregar estudiante ---");
        try {
            String rut = Validador.normalizarRut(leerTexto("RUT (ej. 12.345.678-5)"));
            if (control.getEstudiantes().containsKey(rut)) {
                throw new RegistroDuplicadoException("Ya existe un estudiante con RUT " + rut + ".");
            }
            String nombre = leerTexto("Nombre completo");
            String carrera = leerTexto("Carrera");
            int anio = leerEntero("Año de ingreso");
            Estudiante e = new Estudiante(rut, nombre, carrera, anio);
            control.registrarEstudiante(e);
            System.out.println("Estudiante registrado: " + e);
        } catch (DatoInvalidoException | RegistroDuplicadoException ex) {
            error(ex);
        }
    }

    private void mostrarEstudiantes(List<Estudiante> lista) {
        if (lista.isEmpty()) {
            System.out.println("No hay estudiantes para mostrar.");
            return;
        }
        System.out.printf("%-13s | %-28s | %-30s | %-4s | %s%n", "RUT", "NOMBRE", "CARRERA", "AÑO", "ESTADO");
        System.out.println(linea(95));
        for (Estudiante e : lista) {
            System.out.printf("%-13s | %-28s | %-30s | %-4d | %s%n", e.getRut(), recortar(e.getNombre(), 28),
                    recortar(e.getCarrera(), 30), e.getAnioIngreso(), e.getEstadoProceso());
        }
        System.out.println("Total: " + lista.size());
    }

    private void editarEstudiante() {
        System.out.println("\n--- Editar estudiante ---");
        try {
            Estudiante e = control.buscarEstudiante(leerTexto("RUT del estudiante"));
            System.out.println("Editando " + e + ". Presione ENTER para mantener el valor actual.");
            String nombre = leerConDefecto("Nombre", e.getNombre());
            String carrera = leerConDefecto("Carrera", e.getCarrera());
            int anio = Validador.entero(leerConDefecto("Año de ingreso", String.valueOf(e.getAnioIngreso())), "Año de ingreso");
            EstadoProceso estado = elegir("Estado del proceso (actual: " + e.getEstadoProceso() + ")",
                    Arrays.asList(EstadoProceso.values()), e.getEstadoProceso());
            control.editarEstudiante(e.getRut(), nombre, carrera, anio, estado);
            System.out.println("Estudiante actualizado.");
        } catch (EstudianteNoEncontradoException | DatoInvalidoException ex) {
            error(ex);
        }
    }

    private void eliminarEstudiante() {
        System.out.println("\n--- Eliminar estudiante ---");
        try {
            Estudiante e = control.buscarEstudiante(leerTexto("RUT del estudiante"));
            if (confirmar("¿Eliminar a " + e + " y todos sus trámites?")) {
                int tramites = control.eliminarEstudiante(e.getRut());
                System.out.println("Estudiante eliminado (junto con " + tramites + " trámite(s)).");
            }
        } catch (EstudianteNoEncontradoException ex) {
            error(ex);
        }
    }

    // ================================================================ CONVENIOS

    private void menuConvenios() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n----- CONVENIOS -----");
            System.out.println(" 1. Agregar convenio");
            System.out.println(" 2. Listar convenios");
            System.out.println(" 3. Editar convenio (incluye requisitos)");
            System.out.println(" 4. Eliminar convenio");
            System.out.println(" 5. Buscar convenio");
            System.out.println(" 0. Volver");
            switch (leerOpcion(5)) {
                case 1: agregarConvenio(); break;
                case 2: mostrarConvenios(control.listarConvenios()); break;
                case 3: editarConvenio(); break;
                case 4: eliminarConvenio(); break;
                case 5: mostrarConvenios(control.buscarConvenios(leerTexto("Texto a buscar (ID, nombre, universidad, país o carrera)"))); break;
                default: volver = true;
            }
        }
    }

    private void agregarConvenio() {
        System.out.println("\n--- Agregar convenio ---");
        try {
            String id = leerTexto("ID (ej. IT-POLIMI-2026)");
            String nombre = leerTexto("Nombre");
            String universidad = leerTexto("Universidad socia");
            String pais = leerTexto("País");
            int meses = leerEntero("Duración en meses");
            String carrera = leerTexto("Carrera asociada");
            Set<TipoDocumento> requisitos = elegirRequisitos(EnumSet.noneOf(TipoDocumento.class));
            Convenio c = new Convenio(id, nombre, universidad, pais, meses, carrera, requisitos);
            control.agregarConvenio(c);
            System.out.println("Convenio agregado: " + c);
        } catch (DatoInvalidoException | RegistroDuplicadoException ex) {
            error(ex);
        }
    }

    private void mostrarConvenios(List<Convenio> lista) {
        if (lista.isEmpty()) {
            System.out.println("No hay convenios para mostrar.");
            return;
        }
        for (Convenio c : lista) {
            System.out.println(linea(70));
            System.out.println(c.getIdConvenio() + " - " + c.getNombre());
            System.out.println("  Universidad: " + c.getUniversidadSocia() + " (" + c.getPais() + ")");
            System.out.println("  Duración:    " + c.getDuracionMeses() + " meses   Carrera: " + c.getCarreraAsociada());
            System.out.println("  Requisitos:  " + c.getRequisitos());
            System.out.println("  Trámites:    " + c.getTramites().size());
        }
        System.out.println(linea(70));
        System.out.println("Total: " + lista.size());
    }

    private void editarConvenio() {
        System.out.println("\n--- Editar convenio ---");
        try {
            Convenio c = control.buscarConvenio(leerTexto("ID del convenio"));
            System.out.println("Editando " + c + ". Presione ENTER para mantener el valor actual.");
            String nombre = leerConDefecto("Nombre", c.getNombre());
            String universidad = leerConDefecto("Universidad socia", c.getUniversidadSocia());
            String pais = leerConDefecto("País", c.getPais());
            int meses = Validador.entero(leerConDefecto("Duración en meses", String.valueOf(c.getDuracionMeses())), "Duración");
            String carrera = leerConDefecto("Carrera asociada", c.getCarreraAsociada());
            Set<TipoDocumento> requisitos = c.getRequisitos();
            if (confirmar("¿Modificar los requisitos? (actuales: " + c.getRequisitos() + ")")) {
                requisitos = elegirRequisitos(c.getRequisitos());
            }
            control.editarConvenio(c.getIdConvenio(), nombre, universidad, pais, meses, carrera, requisitos);
            System.out.println("Convenio actualizado.");
        } catch (ConvenioNoEncontradoException | DatoInvalidoException ex) {
            error(ex);
        }
    }

    private void eliminarConvenio() {
        System.out.println("\n--- Eliminar convenio ---");
        try {
            Convenio c = control.buscarConvenio(leerTexto("ID del convenio"));
            if (confirmar("¿Eliminar " + c + " y sus " + c.getTramites().size() + " trámite(s)?")) {
                control.eliminarConvenio(c.getIdConvenio());
                System.out.println("Convenio eliminado.");
            }
        } catch (ConvenioNoEncontradoException ex) {
            error(ex);
        }
    }

    private Set<TipoDocumento> elegirRequisitos(Set<TipoDocumento> actuales) {
        Set<TipoDocumento> elegidos = EnumSet.noneOf(TipoDocumento.class);
        System.out.println("Requisitos del convenio (responda s/n para cada documento):");
        for (TipoDocumento td : TipoDocumento.values()) {
            String marca = actuales.contains(td) ? " [actualmente sí]" : "";
            if (confirmar("  ¿Exigir " + td + "?" + marca)) {
                elegidos.add(td);
            }
        }
        return elegidos;
    }

    // ================================================================ TRÁMITES

    private void menuTramites() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n----- TRÁMITES Y DOCUMENTOS -----");
            System.out.println(" 1. Crear trámite");
            System.out.println(" 2. Listar trámites");
            System.out.println(" 3. Ver detalle de un trámite");
            System.out.println(" 4. Editar estado de un trámite (aprobar / rechazar / reabrir)");
            System.out.println(" 5. Eliminar trámite");
            System.out.println(" 6. Buscar trámite");
            System.out.println(" 7. Subir documento a un trámite");
            System.out.println(" 8. Quitar documento de un trámite");
            System.out.println(" 0. Volver");
            switch (leerOpcion(8)) {
                case 1: crearTramite(); break;
                case 2: listarTramites(); break;
                case 3: verDetalleTramite(); break;
                case 4: editarTramite(); break;
                case 5: eliminarTramite(); break;
                case 6: mostrarTramites(control.buscarTramites(leerTexto("Texto a buscar (ID, RUT, nombre o convenio)"))); break;
                case 7: subirDocumento(); break;
                case 8: quitarDocumento(); break;
                default: volver = true;
            }
        }
    }

    private void crearTramite() {
        System.out.println("\n--- Crear trámite ---");
        try {
            Convenio c = elegir("Convenio", control.listarConvenios(), null);
            if (c == null) {
                return;
            }
            String rut = leerTexto("RUT del estudiante");
            Tramite t = control.crearTramite(c.getIdConvenio(), rut);
            System.out.println("Trámite creado: " + t.getIdTramite() + ". Documentos exigidos: " + c.getRequisitos());
        } catch (ConvenioNoEncontradoException | EstudianteNoEncontradoException | RegistroDuplicadoException ex) {
            error(ex);
        }
    }

    private void listarTramites() {
        System.out.println("Listar: 1. Todos   2. De un convenio   3. Por estado");
        try {
            switch (leerOpcion(3)) {
                case 1:
                    mostrarTramites(control.listarTramites());
                    break;
                case 2:
                    Convenio c = elegir("Convenio", control.listarConvenios(), null);
                    if (c != null) {
                        mostrarTramites(control.listarTramites(c.getIdConvenio()));
                    }
                    break;
                case 3:
                    EstadoTramite estado = elegir("Estado", Arrays.asList(EstadoTramite.values()), null);
                    if (estado != null) {
                        mostrarTramites(control.listarTramites(estado));
                    }
                    break;
                default:
                    break;
            }
        } catch (ConvenioNoEncontradoException ex) {
            error(ex);
        }
    }

    private void mostrarTramites(List<Tramite> lista) {
        if (lista.isEmpty()) {
            System.out.println("No hay trámites para mostrar.");
            return;
        }
        System.out.printf("%-7s | %-13s | %-13s | %-26s | %-10s | %s%n",
                "ID", "CONVENIO", "RUT", "ESTUDIANTE", "ESTADO", "DOCS");
        System.out.println(linea(90));
        for (Tramite t : lista) {
            System.out.printf("%-7s | %-13s | %-13s | %-26s | %-10s | %d/%d%n", t.getIdTramite(),
                    t.getConvenio().getIdConvenio(), t.getEstudiante().getRut(),
                    recortar(t.getEstudiante().getNombre(), 26), t.getEstado(),
                    t.getDocumentos().size(), t.getConvenio().getRequisitos().size());
        }
        System.out.println("Total: " + lista.size());
    }

    private void verDetalleTramite() {
        try {
            Tramite t = control.buscarTramite(leerTexto("ID del trámite (ej. T-0001)"));
            System.out.println(linea(60));
            System.out.println("Trámite:     " + t.getIdTramite() + "   Estado: " + t.getEstado());
            System.out.println("Estudiante:  " + t.getEstudiante());
            System.out.println("Convenio:    " + t.getConvenio() + " - " + t.getConvenio().getUniversidadSocia());
            System.out.println("Documentos:");
            for (TipoDocumento req : t.getConvenio().getRequisitos()) {
                DocumentoSubido d = t.getDocumentos().get(req);
                System.out.println("  [" + (d == null ? " " : "X") + "] " + req
                        + (d == null ? "  (pendiente)" : "  " + d.getNombreArchivo() + " - " + d.getFechaSubida()));
            }
            if (!t.getObservacion().isEmpty()) {
                System.out.println("Observación: " + t.getObservacion());
            }
            System.out.println(linea(60));
        } catch (TramiteNoEncontradoException ex) {
            error(ex);
        }
    }

    private void editarTramite() {
        System.out.println("\n--- Editar estado de trámite ---");
        try {
            Tramite t = control.buscarTramite(leerTexto("ID del trámite"));
            System.out.println("Estado actual: " + t.getEstado());
            EstadoTramite estado = elegir("Nuevo estado", Arrays.asList(EstadoTramite.values()), t.getEstado());
            String observacion = leerConDefecto("Observación", t.getObservacion());
            control.editarTramite(t.getIdTramite(), estado, observacion);
            System.out.println("Trámite actualizado: " + t + ". Estado del estudiante: "
                    + t.getEstudiante().getEstadoProceso());
        } catch (TramiteNoEncontradoException | DatoInvalidoException ex) {
            error(ex);
        }
    }

    private void eliminarTramite() {
        System.out.println("\n--- Eliminar trámite ---");
        try {
            Tramite t = control.buscarTramite(leerTexto("ID del trámite"));
            if (confirmar("¿Eliminar " + t + "?")) {
                control.eliminarTramite(t.getIdTramite());
                System.out.println("Trámite eliminado.");
            }
        } catch (TramiteNoEncontradoException ex) {
            error(ex);
        }
    }

    private void subirDocumento() {
        System.out.println("\n--- Subir documento ---");
        try {
            Tramite t = control.buscarTramite(leerTexto("ID del trámite"));
            List<TipoDocumento> exigidos = new ArrayList<>(t.getConvenio().getRequisitos());
            TipoDocumento tipo = elegir("Documento (faltan: " + t.getDocumentosFaltantes() + ")", exigidos, null);
            if (tipo == null) {
                return;
            }
            String archivo = leerTexto("Nombre del archivo (ej. pasaporte.pdf)");
            try {
                control.subirDocumento(t.getIdTramite(), tipo, archivo, false);
            } catch (DocumentoDuplicadoException dup) {
                System.out.println(dup.getMessage());
                if (!confirmar("¿Desea reemplazarlo?")) {
                    return;
                }
                control.subirDocumento(t.getIdTramite(), tipo, archivo, true);
            }
            System.out.println("Documento subido. Estado del trámite: " + t.getEstado());
        } catch (TramiteNoEncontradoException | DocumentoDuplicadoException | DatoInvalidoException ex) {
            error(ex);
        }
    }

    private void quitarDocumento() {
        System.out.println("\n--- Quitar documento ---");
        try {
            Tramite t = control.buscarTramite(leerTexto("ID del trámite"));
            List<TipoDocumento> subidos = new ArrayList<>(t.getDocumentos().keySet());
            if (subidos.isEmpty()) {
                System.out.println("El trámite no tiene documentos subidos.");
                return;
            }
            TipoDocumento tipo = elegir("Documento a quitar", subidos, null);
            if (tipo != null) {
                control.quitarDocumento(t.getIdTramite(), tipo);
                System.out.println("Documento quitado. Estado del trámite: " + t.getEstado());
            }
        } catch (TramiteNoEncontradoException | DatoInvalidoException ex) {
            error(ex);
        }
    }

    // ================================================================ REPORTES

    private void menuReportes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n----- REPORTES DE SEGUIMIENTO -----");
            System.out.println(" 1. Trámites a los que les falta un documento");
            System.out.println(" 2. Avance por convenio");
            System.out.println(" 0. Volver");
            Reporte reporte = null;
            switch (leerOpcion(2)) {
                case 1:
                    TipoDocumento tipo = elegir("Documento", Arrays.asList(TipoDocumento.values()), null);
                    if (tipo == null) {
                        break;
                    }
                    String idConvenio = null;
                    if (confirmar("¿Filtrar por un convenio específico?")) {
                        Convenio c = elegir("Convenio", control.listarConvenios(), null);
                        idConvenio = c == null ? null : c.getIdConvenio();
                    }
                    reporte = new ReporteDocumentoFaltante(tipo, idConvenio);
                    break;
                case 2:
                    reporte = new ReporteAvanceConvenios();
                    break;
                default:
                    volver = true;
            }
            if (reporte != null) {
                System.out.println();
                System.out.println(reporte.generarTexto(control));
            }
        }
    }

    // ================================================================ ARCHIVO

    private void exportarExcel() {
        String nombre = leerConDefecto("Nombre del archivo", "intercambio.xlsx");
        if (!nombre.toLowerCase().endsWith(".xlsx")) {
            nombre += ".xlsx";
        }
        try {
            new ExportadorExcel(control).exportar(Paths.get(nombre));
            System.out.println("Planilla creada en " + Paths.get(nombre).toAbsolutePath());
        } catch (IOException ex) {
            System.out.println("No se pudo crear la planilla: " + ex.getMessage());
        }
    }

    private void guardar() {
        try {
            dataStore.guardar();
            System.out.println("Datos guardados en " + dataStore.getCarpeta().toAbsolutePath());
        } catch (IOException ex) {
            System.out.println("ERROR al guardar: " + ex.getMessage());
        }
    }

    private void restaurarDatosEjemplo() {
        if (!confirmar("Se reemplazarán TODOS los datos actuales por los de ejemplo. ¿Continuar?")) {
            return;
        }
        try {
            control.cargarDatosIniciales();
            System.out.println("Datos de ejemplo cargados.");
        } catch (DatoInvalidoException | RegistroDuplicadoException ex) {
            error(ex);
        }
    }

    private void mostrarAdvertenciasDeCarga() {
        List<String> advertencias = dataStore.getAdvertencias();
        if (!advertencias.isEmpty()) {
            System.out.println("Advertencias al cargar los datos:");
            for (String a : advertencias) {
                System.out.println("  - " + a);
            }
        }
        System.out.println("Datos cargados: " + control.listarEstudiantes().size() + " estudiantes, "
                + control.listarConvenios().size() + " convenios, " + control.listarTramites().size() + " trámites.");
    }

    // ================================================================ LECTURA DE DATOS

    /** Se lanza si se cierra la entrada estándar (por ejemplo, Ctrl+D). */
    private static class EntradaCerradaException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje + ": ");
        if (!entrada.hasNextLine()) {
            throw new EntradaCerradaException();
        }
        return entrada.nextLine().trim();
    }

    private String leerConDefecto(String mensaje, String actual) {
        String valor = leerTexto(mensaje + " [" + actual + "]");
        return valor.isEmpty() ? actual : valor;
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Validador.entero(leerTexto(mensaje), mensaje);
            } catch (DatoInvalidoException ex) {
                System.out.println("  " + ex.getMessage());
            }
        }
    }

    private int leerOpcion(int maximo) {
        while (true) {
            int opcion = leerEntero("Opción");
            if (opcion >= 0 && opcion <= maximo) {
                return opcion;
            }
            System.out.println("  Opción inválida: ingrese un número entre 0 y " + maximo + ".");
        }
    }

    private boolean confirmar(String pregunta) {
        String r = leerTexto(pregunta + " (s/n)").toLowerCase();
        return r.equals("s") || r.equals("si") || r.equals("sí");
    }

    /**
     * Muestra una lista numerada y devuelve el elemento elegido.
     * Con ENTER devuelve el valor por defecto; con 0, null (cancelar).
     */
    private <T> T elegir(String titulo, List<T> opciones, T porDefecto) {
        if (opciones.isEmpty()) {
            System.out.println("No hay opciones disponibles.");
            return null;
        }
        System.out.println(titulo + ":");
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + opciones.get(i));
        }
        System.out.println("  0. Cancelar");
        while (true) {
            String texto = leerTexto(porDefecto == null ? "Número" : "Número [ENTER = " + porDefecto + "]");
            if (texto.isEmpty() && porDefecto != null) {
                return porDefecto;
            }
            try {
                int n = Validador.entero(texto, "Número");
                if (n == 0) {
                    return null;
                }
                if (n >= 1 && n <= opciones.size()) {
                    return opciones.get(n - 1);
                }
                System.out.println("  Ingrese un número entre 0 y " + opciones.size() + ".");
            } catch (DatoInvalidoException ex) {
                System.out.println("  " + ex.getMessage());
            }
        }
    }

    private void error(Exception ex) {
        System.out.println("ERROR: " + ex.getMessage());
    }

    private static String linea(int largo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < largo; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }
}
