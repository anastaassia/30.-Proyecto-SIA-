package com.mycompany.mavenproject1;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore 
{
    private final Control control;
    
    private final Path base = Paths.get("data");
    private final Path fEst = base.resolve("estudiantes.csv");
    private final Path fCon = base.resolve("convenios.csv");
    private final Path fTra = base.resolve("tramites.csv");
    private final Path fDoc = base.resolve("documentos.csv");

    public DataStore(Control control) {
        this.control = control;
    }
    
        // API publica
    public void load() throws IOException 
    {
        if (!Files.exists(base)) Files.createDirectories(base);
        cargarConvenios();
        cargarEstudiantes();
        cargarTramitesYDocs();
    }

    public void save() throws IOException 
    {
        if (!Files.exists(base)) Files.createDirectories(base);
        guardarConvenios();
        guardarEstudiantes();
        guardarTramitesYDocs();
    }

    // CARGA
    private void cargarConvenios() throws IOException 
    {
        if (!Files.exists(fCon)) return;
        for (String ln : Files.readAllLines(fCon)) 
        {
            if (ln.isBlank() || ln.startsWith("#")) continue;
            // id, nombre, universidad, pais, duracion, carrera, req1, req2 y asi
            String[] p = ln.split(";");
            if (p.length < 7) continue;

            String id         = p[0];
            String nombre     = p[1];
            String uni        = p[2];
            String pais       = p[3];
            String duracion   = p[4];
            String carrera    = p[5];

            Set<TipoDocumento> req = new HashSet<>();
            if (!p[6].isBlank()) 
            {
                for (String tok : p[6].split(",")) 
                {
                    req.add(TipoDocumento.valueOf(tok.trim()));
                }
            }

            control.agregarConvenio(new Convenio(id, nombre, uni, pais, req, duracion, carrera));
        }
    }

    private void cargarEstudiantes() throws IOException 
    {
        if (!Files.exists(fEst)) return;
        for (String ln : Files.readAllLines(fEst)) 
        {
            if (ln.isBlank() || ln.startsWith("#")) continue;
            // rut, nombre, carrera, anio, estado, idConvenioActual(no es obligatorio)
            String[] p = ln.split(";");
            if (p.length < 6) continue;

            String rut     = p[0];
            String nombre  = p[1];
            String carrera = p[2];
            int anio       = Integer.parseInt(p[3]);
            String estado  = p[4];
            String idConv  = p[5];

            control.registrarEstudiante(rut, nombre, carrera, anio);
            Estudiante e = control.buscarEstudiante(rut);
            if (e != null) 
            {
                e.setEstadoProceso(estado);
                if (!idConv.isBlank()) 
                {
                    Convenio c = control.buscarConvenio(idConv);
                    if (c != null) e.setConvenio(c);
                }
            }
        }
    }

    private void cargarTramitesYDocs() throws IOException 
    {
        if (!Files.exists(fTra)) return;

        Map<String, Tramite> idx = new HashMap<>();
        for (String ln : Files.readAllLines(fTra)) 
        {
            if (ln.isBlank() || ln.startsWith("#")) continue;
            // idTramite;idConvenio;rut;estado
            String[] p = ln.split(";");
            if (p.length < 4) continue;

            String idT = p[0];
            String idC = p[1];
            String rut = p[2];
            String est = p[3];

            Convenio c = control.buscarConvenio(idC);
            Estudiante e = control.buscarEstudiante(rut);
            if (c == null || e == null) continue;

            Tramite t = new Tramite(idT, e);
            if ("COMPLETO".equalsIgnoreCase(est)) t.setEstado(Tramite.Estado.COMPLETO);
            c.getTramites().add(t);
            idx.put(idT, t);
        }

        if (Files.exists(fDoc)) 
        {
            for (String ln : Files.readAllLines(fDoc)) 
            {
                if (ln.isBlank() || ln.startsWith("#")) continue;
                // idTramite, tipo, nombreArchivo, fecha
                String[] p = ln.split(";");
                if (p.length < 4) continue;

                Tramite t = idx.get(p[0]);
                if (t == null) continue;

                TipoDocumento tipo = TipoDocumento.valueOf(p[1]);
                String nombreArch  = p[2];
                LocalDate fecha    = LocalDate.parse(p[3]);

                // tu Tramite tiene subirDocumento(tipo, nombre)
                t.subirDocumento(tipo, nombreArch);

                // y ahora podemos ajustar la fecha en el objeto creado:
                DocumentoSubido d = t.getDocumentos().get(tipo); // <- getter correcto
                if (d != null) d.setFechaSubida(fecha);
            }
        }
    }

    // GUARDADO
    private void guardarConvenios() throws IOException 
    {
        try (BufferedWriter w = Files.newBufferedWriter(fCon)) 
        {
            for (Convenio c : control.getConvenios()) 
            {
                String req = String.join(",",
                        c.getRequisitos().stream().map(Enum::name).collect(Collectors.toList())); 
                w.write(String.join(";", Arrays.asList(
                        c.getIdConvenio(),
                        c.getNombre(),
                        c.getUniversidadSocia(),
                        c.getPais(),
                        c.getDuracion(),
                        c.getCarreraAsociada(),
                        req
                )));
                w.newLine();
            }
        }
    }

    private void guardarEstudiantes() throws IOException 
    {
        try (BufferedWriter w = Files.newBufferedWriter(fEst)) 
        {
            for (Estudiante e : control.getEstudiantes()) 
            {
                String idC = (e.getConvenio() == null) ? "" : e.getConvenio().getIdConvenio();
                w.write(String.join(";", Arrays.asList(
                        e.getRut(),
                        e.getNombre(),
                        e.getCarrera(),
                        String.valueOf(e.getAnioIngreso()),
                        e.getEstadoProceso() == null ? "" : e.getEstadoProceso(),
                        idC
                )));
                w.newLine();
            }
        }
    }

    private void guardarTramitesYDocs() throws IOException 
    {
        try (BufferedWriter wt = Files.newBufferedWriter(fTra);
            BufferedWriter wd = Files.newBufferedWriter(fDoc)) {

            for (Convenio c : control.getConvenios()) 
            {
                for (Tramite t : c.getTramites()) 
                {
                    wt.write(String.join(";", Arrays.asList(
                            t.getIdTramite(),
                            c.getIdConvenio(),
                            t.getEstudiante().getRut(),
                            t.getEstado().name()
                    )));
                    wt.newLine();

                    // USAR getDocumentosParaPersistencia() en lugar de getDocumentos()
                    for (Map.Entry<TipoDocumento, DocumentoSubido> entry : t.getDocumentosParaPersistencia().entrySet()) {
                        DocumentoSubido d = entry.getValue();
                        wd.write(String.join(";", Arrays.asList(
                                t.getIdTramite(),
                                entry.getKey().name(),
                                d.getNombreArchivo(),
                                d.getFechaSubida().toString()
                        )));
                        wd.newLine();
                    }
                }
            }
        }
    }
}

//La estructura de los datos 
/*
Estos se crean solos
data/estudiantes.csv
data/convenios.csv
data/tramites.csv
data/documentos.csv
*/
