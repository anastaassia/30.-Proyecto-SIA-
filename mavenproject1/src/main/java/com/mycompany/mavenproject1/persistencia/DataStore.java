package com.mycompany.mavenproject1.persistencia;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.control.Validador;
import com.mycompany.mavenproject1.excepciones.ConvenioNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
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
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persistencia batch en archivos CSV (separador ';' y codificación UTF-8):
 * se carga todo al iniciar la aplicación y se graba todo al salir.
 * <p>
 * Cada archivo tiene una fila de cabecera. La carga es tolerante: si una línea
 * está mal escrita (por ejemplo, al editar el CSV a mano o con Excel), esa línea se
 * omite y se registra una advertencia, pero el resto de los datos se carga igual.
 * <ul>
 *   <li>convenios.csv: idConvenio;nombre;universidadSocia;pais;duracionMeses;carreraAsociada;requisitos</li>
 *   <li>estudiantes.csv: rut;nombre;carrera;anioIngreso;estadoProceso</li>
 *   <li>tramites.csv: idTramite;idConvenio;rutEstudiante;estado;observacion</li>
 *   <li>documentos.csv: idTramite;tipoDocumento;nombreArchivo;fechaSubida</li>
 * </ul>
 */
public class DataStore {

    /** Separador de columnas. */
    public static final String SEPARADOR = ";";
    /** Separador de la lista de requisitos dentro de una columna. */
    public static final String SEPARADOR_LISTA = ",";

    static final String[] CABECERA_CONVENIOS =
            {"idConvenio", "nombre", "universidadSocia", "pais", "duracionMeses", "carreraAsociada", "requisitos"};
    static final String[] CABECERA_ESTUDIANTES =
            {"rut", "nombre", "carrera", "anioIngreso", "estadoProceso"};
    static final String[] CABECERA_TRAMITES =
            {"idTramite", "idConvenio", "rutEstudiante", "estado", "observacion"};
    static final String[] CABECERA_DOCUMENTOS =
            {"idTramite", "tipoDocumento", "nombreArchivo", "fechaSubida"};

    private Control control;
    private Path carpeta;
    private List<String> advertencias = new ArrayList<>();

    /**
     * Usa la carpeta "data" dentro del directorio de trabajo (en NetBeans, la carpeta del proyecto).
     *
     * @param control controlador donde se cargan los datos
     */
    public DataStore(Control control) {
        this(control, Paths.get("data"));
    }

    /**
     * @param control controlador donde se cargan los datos
     * @param carpeta carpeta donde están los archivos CSV
     */
    public DataStore(Control control, Path carpeta) {
        this.control = control;
        this.carpeta = carpeta;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public Path getCarpeta() {
        return carpeta;
    }

    public void setCarpeta(Path carpeta) {
        this.carpeta = carpeta;
    }

    /** @return advertencias de la última carga (líneas omitidas y su motivo) */
    public List<String> getAdvertencias() {
        return Collections.unmodifiableList(advertencias);
    }

    /** @return true si existe al menos uno de los archivos CSV */
    public boolean existenArchivos() {
        return Files.exists(archivo("convenios.csv")) || Files.exists(archivo("estudiantes.csv"))
                || Files.exists(archivo("tramites.csv")) || Files.exists(archivo("documentos.csv"));
    }

    private Path archivo(String nombre) {
        return carpeta.resolve(nombre);
    }

    // ================================================================== CARGA

    /**
     * Carga todos los archivos en el controlador (que se limpia antes).
     * Las líneas con errores se omiten y quedan en {@link #getAdvertencias()}.
     *
     * @throws IOException si un archivo existe pero no se puede leer
     */
    public void cargar() throws IOException {
        advertencias = new ArrayList<>();
        control.limpiar();
        cargarConvenios();
        cargarEstudiantes();
        Map<String, EstadoTramite> estadosGuardados = cargarTramites();
        cargarDocumentos();
        restaurarEstados(estadosGuardados);
        ordenarAdvertencias();
    }

    /** Deja las advertencias en el orden de los archivos y, dentro de cada uno, por número de línea. */
    private void ordenarAdvertencias() {
        final List<String> archivos = java.util.Arrays.asList("convenios.csv", "estudiantes.csv",
                "tramites.csv", "documentos.csv");
        advertencias.sort(java.util.Comparator
                .comparingInt((String a) -> archivos.indexOf(a.split(",|:")[0].trim()))
                .thenComparingInt(DataStore::numeroDeLinea));
    }

    private static int numeroDeLinea(String advertencia) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("línea (\\d+)").matcher(advertencia);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private void cargarConvenios() throws IOException {
        for (Fila f : leer("convenios.csv", CABECERA_CONVENIOS)) {
            try {
                Set<TipoDocumento> requisitos = EnumSet.noneOf(TipoDocumento.class);
                for (String req : f.campo(6).split(SEPARADOR_LISTA)) {
                    if (!req.trim().isEmpty()) {
                        requisitos.add(convertirEnum(TipoDocumento.class, req, "tipo de documento"));
                    }
                }
                int meses = Validador.entero(f.campo(4), "duracionMeses");
                control.agregarConvenio(new Convenio(f.campo(0), f.campo(1), f.campo(2), f.campo(3),
                        meses, f.campo(5), requisitos));
            } catch (DatoInvalidoException | RegistroDuplicadoException e) {
                advertir(f, e.getMessage());
            }
        }
    }

    private void cargarEstudiantes() throws IOException {
        for (Fila f : leer("estudiantes.csv", CABECERA_ESTUDIANTES)) {
            try {
                int anio = Validador.entero(f.campo(3), "anioIngreso");
                Estudiante e = new Estudiante(f.campo(0), f.campo(1), f.campo(2), anio);
                if (!f.campo(4).isEmpty()) {
                    e.setEstadoProceso(convertirEnum(EstadoProceso.class, f.campo(4), "estado del proceso"));
                }
                control.registrarEstudiante(e);
            } catch (DatoInvalidoException | RegistroDuplicadoException e) {
                advertir(f, e.getMessage());
            }
        }
    }

    /**
     * Carga los trámites. Los estados APROBADO/RECHAZADO se devuelven aparte para
     * aplicarlos después de cargar los documentos (los demás estados se recalculan).
     */
    private Map<String, EstadoTramite> cargarTramites() throws IOException {
        Map<String, EstadoTramite> estados = new HashMap<>();
        for (Fila f : leer("tramites.csv", CABECERA_TRAMITES)) {
            try {
                String idTramite = Validador.textoObligatorio(f.campo(0), "idTramite");
                Estudiante e = control.buscarEstudiante(f.campo(2));
                Convenio c = control.buscarConvenio(f.campo(1));
                Tramite t = new Tramite(idTramite, e, c);
                t.setObservacion(f.campo(4));
                control.agregarTramite(c.getIdConvenio(), t);
                if (!f.campo(3).isEmpty()) {
                    estados.put(idTramite, convertirEnum(EstadoTramite.class, f.campo(3), "estado del trámite"));
                }
            } catch (DatoInvalidoException | EstudianteNoEncontradoException
                     | ConvenioNoEncontradoException | RegistroDuplicadoException e) {
                advertir(f, e.getMessage());
            }
        }
        return estados;
    }

    private void cargarDocumentos() throws IOException {
        for (Fila f : leer("documentos.csv", CABECERA_DOCUMENTOS)) {
            try {
                Tramite t = control.buscarTramite(f.campo(0));
                TipoDocumento tipo = convertirEnum(TipoDocumento.class, f.campo(1), "tipo de documento");
                String archivo = Validador.textoObligatorio(f.campo(2), "nombreArchivo");
                LocalDate fecha = f.campo(3).isEmpty() ? LocalDate.now() : LocalDate.parse(f.campo(3));
                t.subirDocumento(tipo, archivo, fecha);
            } catch (TramiteNoEncontradoException | DatoInvalidoException e) {
                advertir(f, e.getMessage());
            } catch (DateTimeParseException e) {
                advertir(f, "fecha inválida \"" + f.campo(3) + "\" (formato esperado AAAA-MM-DD)");
            }
        }
    }

    private void restaurarEstados(Map<String, EstadoTramite> estados) {
        for (Map.Entry<String, EstadoTramite> entrada : estados.entrySet()) {
            if (!entrada.getValue().esResuelto()) {
                continue; // EN_PROCESO / COMPLETO ya quedaron calculados según los documentos
            }
            try {
                control.buscarTramite(entrada.getKey()).cambiarEstado(entrada.getValue());
            } catch (TramiteNoEncontradoException | DatoInvalidoException e) {
                advertencias.add("tramites.csv, trámite " + entrada.getKey() + ": " + e.getMessage()
                        + " Se dejó en su estado calculado.");
            }
        }
    }

    // ================================================================== GUARDADO

    /**
     * Graba todos los datos del controlador, reemplazando los archivos.
     *
     * @throws IOException si no se puede escribir en la carpeta de datos
     */
    public void guardar() throws IOException {
        Files.createDirectories(carpeta);

        List<String[]> convenios = new ArrayList<>();
        List<String[]> tramites = new ArrayList<>();
        List<String[]> documentos = new ArrayList<>();
        for (Convenio c : control.listarConvenios()) {
            List<String> requisitos = new ArrayList<>();
            for (TipoDocumento td : c.getRequisitos()) {
                requisitos.add(td.name());
            }
            convenios.add(new String[]{c.getIdConvenio(), c.getNombre(), c.getUniversidadSocia(), c.getPais(),
                String.valueOf(c.getDuracionMeses()), c.getCarreraAsociada(), String.join(SEPARADOR_LISTA, requisitos)});
            for (Tramite t : c.getTramites()) {
                tramites.add(new String[]{t.getIdTramite(), c.getIdConvenio(), t.getEstudiante().getRut(),
                    t.getEstado().name(), t.getObservacion()});
                for (DocumentoSubido d : t.getDocumentos().values()) {
                    documentos.add(new String[]{t.getIdTramite(), d.getTipo().name(), d.getNombreArchivo(),
                        d.getFechaSubida().toString()});
                }
            }
        }
        List<String[]> estudiantes = new ArrayList<>();
        for (Estudiante e : control.listarEstudiantes()) {
            estudiantes.add(new String[]{e.getRut(), e.getNombre(), e.getCarrera(),
                String.valueOf(e.getAnioIngreso()), e.getEstadoProceso().name()});
        }

        escribir("convenios.csv", CABECERA_CONVENIOS, convenios);
        escribir("estudiantes.csv", CABECERA_ESTUDIANTES, estudiantes);
        escribir("tramites.csv", CABECERA_TRAMITES, tramites);
        escribir("documentos.csv", CABECERA_DOCUMENTOS, documentos);
    }

    /**
     * Escribe primero en un archivo temporal y luego lo mueve, para que un error a
     * mitad de la escritura no deje el CSV original vacío o cortado.
     */
    private void escribir(String nombre, String[] cabecera, List<String[]> filas) throws IOException {
        Path destino = archivo(nombre);
        Path temporal = archivo(nombre + ".tmp");
        try (BufferedWriter w = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
            w.write(String.join(SEPARADOR, cabecera));
            w.newLine();
            for (String[] fila : filas) {
                String[] limpia = new String[fila.length];
                for (int i = 0; i < fila.length; i++) {
                    limpia[i] = fila[i] == null ? "" : fila[i].replace(SEPARADOR, ",").replaceAll("[\\r\\n]+", " ");
                }
                w.write(String.join(SEPARADOR, limpia));
                w.newLine();
            }
        }
        Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    // ================================================================== LECTURA DE CSV

    /** Línea de datos de un CSV, con su número para los mensajes de advertencia. */
    private static final class Fila {
        private final String archivo;
        private final int numero;
        private final String[] campos;

        private Fila(String archivo, int numero, String[] campos) {
            this.archivo = archivo;
            this.numero = numero;
            this.campos = campos;
        }

        /** @return el campo i sin espacios ni comillas, o "" si la línea tiene menos columnas */
        private String campo(int i) {
            if (i >= campos.length) {
                return "";
            }
            String valor = campos[i].trim();
            if (valor.length() >= 2 && valor.startsWith("\"") && valor.endsWith("\"")) {
                valor = valor.substring(1, valor.length() - 1).replace("\"\"", "\"").trim();
            }
            return valor;
        }
    }

    /**
     * Lee un CSV y devuelve sus filas de datos. Se salta la cabecera, las líneas vacías
     * y las que empiezan con '#'. Las líneas con menos columnas de las esperadas se informan.
     * Si el archivo no es UTF-8 (por ejemplo, guardado con Excel en Windows), se lee como ISO-8859-1.
     */
    private List<Fila> leer(String nombre, String[] cabecera) throws IOException {
        List<Fila> filas = new ArrayList<>();
        Path ruta = archivo(nombre);
        if (!Files.exists(ruta)) {
            advertencias.add(nombre + ": no existe, se omite.");
            return filas;
        }
        List<String> lineas;
        try {
            lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            lineas = Files.readAllLines(ruta, StandardCharsets.ISO_8859_1);
        }
        boolean cabeceraLeida = false;
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).replace("\uFEFF", "");
            if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                continue;
            }
            if (!cabeceraLeida) {
                cabeceraLeida = true;
                if (linea.trim().toLowerCase().startsWith(cabecera[0].toLowerCase() + SEPARADOR)) {
                    continue;
                }
                advertencias.add(nombre + ": no tiene fila de cabecera; se leyó igual.");
            }
            String[] campos = linea.split(SEPARADOR, -1);
            Fila fila = new Fila(nombre, i + 1, campos);
            int minimas = cabecera.length - 1; // la última columna puede venir vacía
            if (campos.length < minimas) {
                advertir(fila, "se esperaban " + cabecera.length + " columnas separadas por '" + SEPARADOR
                        + "' y hay " + campos.length);
                continue;
            }
            filas.add(fila);
        }
        return filas;
    }

    private void advertir(Fila f, String motivo) {
        String texto = motivo.endsWith(".") ? motivo : motivo + ".";
        advertencias.add(f.archivo + ", línea " + f.numero + ": " + texto + " Se omitió la línea.");
    }

    /** Convierte texto a constante de un enum aceptando "PASAPORTE", "pasaporte" o "Pasaporte". */
    private static <E extends Enum<E>> E convertirEnum(Class<E> tipo, String texto, String campo)
            throws DatoInvalidoException {
        String buscado = texto.trim();
        for (E constante : tipo.getEnumConstants()) {
            if (constante.name().equalsIgnoreCase(buscado) || constante.toString().equalsIgnoreCase(buscado)) {
                return constante;
            }
        }
        throw new DatoInvalidoException("valor de " + campo + " desconocido \"" + texto + "\".");
    }
}
