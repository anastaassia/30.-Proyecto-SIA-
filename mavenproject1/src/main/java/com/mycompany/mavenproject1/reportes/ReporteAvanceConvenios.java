package com.mycompany.mavenproject1.reportes;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.EstadoTramite;
import java.util.ArrayList;
import java.util.List;

/**
 * Resumen por convenio: cuántos trámites hay en cada estado y qué porcentaje
 * tiene la documentación completa. Sus datos también alimentan el gráfico de la ventana.
 */
public class ReporteAvanceConvenios extends Reporte {

    /** Crea el reporte de avance. */
    public ReporteAvanceConvenios() {
        super("Avance por convenio");
    }

    @Override
    public String getDescripcion() {
        return "Cantidad de trámites por estado en cada convenio y porcentaje con documentación completa.";
    }

    @Override
    protected List<String> generarLineas(Control control) {
        List<String> lineas = new ArrayList<>();
        List<Convenio> convenios = control.listarConvenios();
        if (convenios.isEmpty()) {
            return lineas;
        }
        lineas.add(String.format("%-14s %6s %10s %9s %9s %10s %8s",
                "CONVENIO", "TOTAL", "EN PROCESO", "COMPLETO", "APROBADO", "RECHAZADO", "% DOCS"));
        for (Convenio c : convenios) {
            int total = c.getTramites().size();
            int conDocumentos = c.contarTramites(EstadoTramite.COMPLETO)
                    + c.contarTramites(EstadoTramite.APROBADO);
            int porcentaje = total == 0 ? 0 : Math.round(conDocumentos * 100f / total);
            lineas.add(String.format("%-14s %6d %10d %9d %9d %10d %7d%%",
                    c.getIdConvenio(), total,
                    c.contarTramites(EstadoTramite.EN_PROCESO),
                    c.contarTramites(EstadoTramite.COMPLETO),
                    c.contarTramites(EstadoTramite.APROBADO),
                    c.contarTramites(EstadoTramite.RECHAZADO),
                    porcentaje));
        }
        return lineas;
    }

    @Override
    protected String mensajeSinResultados() {
        return "No hay convenios registrados.";
    }
}
