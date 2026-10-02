package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import com.mycompany.mavenproject1.reportes.Reporte;
import com.mycompany.mavenproject1.reportes.ReporteAvanceConvenios;
import com.mycompany.mavenproject1.reportes.ReporteDocumentoFaltante;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * Panel de reportes de seguimiento (funcionalidad propia, SIA-9) con el gráfico
 * estadístico (SIA-O1). Ambos reportes se generan a través de la clase padre {@link Reporte}.
 */
public class PanelReportes extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final String TODOS = "Todos los convenios";
    private static final String FALTANTES = "Trámites a los que les falta un documento";
    private static final String AVANCE = "Avance por convenio";

    private final JComboBox<String> cmbReporte = new JComboBox<>(new String[]{FALTANTES, AVANCE});
    private final JComboBox<TipoDocumento> cmbDocumento = new JComboBox<>(TipoDocumento.values());
    private final JComboBox<Object> cmbConvenio = new JComboBox<>();
    private final JTextArea txtResultado = new JTextArea();
    private final GraficoBarras grafico;
    private boolean actualizandoCombos;

    /**
     * @param control controlador del sistema
     */
    public PanelReportes(Control control) {
        super(control, "Reportes de seguimiento",
                "Filtre los trámites pendientes de un documento para saber a quién contactar, o revise el avance por convenio.");
        grafico = new GraficoBarras(control);

        JPanel fila1 = crearBarra();
        fila1.add(new JLabel("Reporte:"));
        fila1.add(cmbReporte);
        fila1.add(crearBoton("Generar", e -> generar()));
        JPanel fila2 = crearBarra();
        fila2.add(new JLabel("Documento:"));
        fila2.add(cmbDocumento);
        fila2.add(new JLabel("  Convenio:"));
        cmbConvenio.setPrototypeDisplayValue("MX-UNAM-2026 - Semestre en la UNAM");
        fila2.add(cmbConvenio);
        JPanel barra = new JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        barra.setOpaque(false);
        barra.add(fila1);
        barra.add(fila2);
        cmbReporte.addActionListener(e -> {
            boolean filtros = FALTANTES.equals(cmbReporte.getSelectedItem());
            cmbDocumento.setEnabled(filtros);
            cmbConvenio.setEnabled(filtros);
            generar();
        });
        cmbDocumento.addActionListener(e -> generar());
        cmbConvenio.addActionListener(e -> {
            if (!actualizandoCombos) {
                generar();
            }
        });
        cmbDocumento.setSelectedItem(TipoDocumento.CERTIFICADO_IDIOMA);

        txtResultado.setEditable(false);
        txtResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtResultado.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(txtResultado), grafico);
        division.setResizeWeight(0.68);
        division.setBorder(null);

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setOpaque(false);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(division, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        actualizandoCombos = true;
        Object elegido = cmbConvenio.getSelectedItem();
        cmbConvenio.removeAllItems();
        cmbConvenio.addItem(TODOS);
        for (Convenio c : getControl().listarConvenios()) {
            cmbConvenio.addItem(c);
        }
        cmbConvenio.setSelectedItem(elegido == null ? TODOS : elegido);
        actualizandoCombos = false;
        generar();
    }

    /** Construye el reporte elegido y lo muestra; el gráfico se redibuja con los datos actuales. */
    private void generar() {
        Reporte reporte;
        if (AVANCE.equals(cmbReporte.getSelectedItem())) {
            reporte = new ReporteAvanceConvenios();
        } else {
            Object convenio = cmbConvenio.getSelectedItem();
            String idConvenio = convenio instanceof Convenio ? ((Convenio) convenio).getIdConvenio() : null;
            reporte = new ReporteDocumentoFaltante((TipoDocumento) cmbDocumento.getSelectedItem(), idConvenio);
        }
        txtResultado.setText(reporte.generarTexto(getControl()));
        txtResultado.setCaretPosition(0);
        grafico.repaint();
    }
}
