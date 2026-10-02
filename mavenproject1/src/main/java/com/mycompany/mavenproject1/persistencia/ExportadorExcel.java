package com.mycompany.mavenproject1.persistencia;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.Estudiante;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import com.mycompany.mavenproject1.modelo.Tramite;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * SIA-O2: genera una planilla Excel (.xlsx) con tres hojas: Convenios, Estudiantes y Trámites.
 * <p>
 * Un archivo .xlsx es un ZIP con archivos XML (formato Office Open XML), por lo que se
 * arma solo con java.util.zip, sin librerías externas. Se abre en Excel, LibreOffice y Google Sheets.
 */
public class ExportadorExcel {

    private Control control;

    /**
     * @param control controlador con los datos a exportar
     */
    public ExportadorExcel(Control control) {
        this.control = control;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    /**
     * Escribe la planilla en la ruta indicada.
     *
     * @param destino archivo .xlsx a crear (se reemplaza si existe)
     * @throws IOException si no se puede escribir el archivo
     */
    public void exportar(Path destino) throws IOException {
        List<List<Object>> convenios = new ArrayList<>();
        convenios.add(fila("ID", "Nombre", "Universidad", "País", "Duración (meses)", "Carrera",
                "Requisitos", "Trámites"));
        for (Convenio c : control.listarConvenios()) {
            List<String> req = new ArrayList<>();
            for (TipoDocumento td : c.getRequisitos()) {
                req.add(td.getEtiqueta());
            }
            convenios.add(fila(c.getIdConvenio(), c.getNombre(), c.getUniversidadSocia(), c.getPais(),
                    c.getDuracionMeses(), c.getCarreraAsociada(), String.join(", ", req), c.getTramites().size()));
        }

        List<List<Object>> estudiantes = new ArrayList<>();
        estudiantes.add(fila("RUT", "Nombre", "Carrera", "Año ingreso", "Estado"));
        for (Estudiante e : control.listarEstudiantes()) {
            estudiantes.add(fila(e.getRut(), e.getNombre(), e.getCarrera(), e.getAnioIngreso(),
                    e.getEstadoProceso().getEtiqueta()));
        }

        List<List<Object>> tramites = new ArrayList<>();
        tramites.add(fila("ID", "Convenio", "RUT", "Estudiante", "Estado", "Documentos subidos",
                "Documentos exigidos", "Faltantes", "Observación"));
        for (Tramite t : control.listarTramites()) {
            tramites.add(fila(t.getIdTramite(), t.getConvenio().getIdConvenio(), t.getEstudiante().getRut(),
                    t.getEstudiante().getNombre(), t.getEstado().getEtiqueta(), t.getDocumentos().size(),
                    t.getConvenio().getRequisitos().size(), t.getDocumentosFaltantes().toString(),
                    t.getObservacion()));
        }

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(destino))) {
            agregar(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + hojaContentType(1) + hojaContentType(2) + hojaContentType(3)
                    + "</Types>");
            agregar(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            agregar(zip, "xl/workbook.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>"
                    + "<sheet name=\"Convenios\" sheetId=\"1\" r:id=\"rId1\"/>"
                    + "<sheet name=\"Estudiantes\" sheetId=\"2\" r:id=\"rId2\"/>"
                    + "<sheet name=\"Tramites\" sheetId=\"3\" r:id=\"rId3\"/>"
                    + "</sheets></workbook>");
            agregar(zip, "xl/_rels/workbook.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + hojaRelacion(1) + hojaRelacion(2) + hojaRelacion(3)
                    + "<Relationship Id=\"rId4\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>");
            agregar(zip, "xl/styles.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                    + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                    + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                    + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill>"
                    + "<fill><patternFill patternType=\"gray125\"/></fill></fills>"
                    + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                    + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                    + "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                    + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/></cellXfs>"
                    + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                    + "</styleSheet>");
            agregar(zip, "xl/worksheets/sheet1.xml", hoja(convenios));
            agregar(zip, "xl/worksheets/sheet2.xml", hoja(estudiantes));
            agregar(zip, "xl/worksheets/sheet3.xml", hoja(tramites));
        }
    }

    private static List<Object> fila(Object... valores) {
        List<Object> fila = new ArrayList<>();
        for (Object v : valores) {
            fila.add(v);
        }
        return fila;
    }

    private static String hojaContentType(int n) {
        return "<Override PartName=\"/xl/worksheets/sheet" + n + ".xml\" "
                + "ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>";
    }

    private static String hojaRelacion(int n) {
        return "<Relationship Id=\"rId" + n + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" "
                + "Target=\"worksheets/sheet" + n + ".xml\"/>";
    }

    /** Arma el XML de una hoja. La primera fila (encabezados) va en negrita. */
    private static String hoja(List<List<Object>> filas) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<cols><col min=\"1\" max=\"10\" width=\"24\" customWidth=\"1\"/></cols><sheetData>");
        for (int r = 0; r < filas.size(); r++) {
            sb.append("<row r=\"").append(r + 1).append("\">");
            List<Object> fila = filas.get(r);
            for (int c = 0; c < fila.size(); c++) {
                String ref = (char) ('A' + c) + String.valueOf(r + 1);
                String estilo = r == 0 ? " s=\"1\"" : "";
                Object valor = fila.get(c);
                if (valor instanceof Number) {
                    sb.append("<c r=\"").append(ref).append("\"").append(estilo).append("><v>")
                      .append(valor).append("</v></c>");
                } else {
                    sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"").append(estilo).append("><is><t>")
                      .append(escaparXml(valor == null ? "" : valor.toString())).append("</t></is></c>");
                }
            }
            sb.append("</row>");
        }
        return sb.append("</sheetData></worksheet>").toString();
    }

    private static String escaparXml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static void agregar(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        OutputStream salida = zip;
        salida.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
