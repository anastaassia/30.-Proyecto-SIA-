package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.EstadoTramite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JPanel;

/**
 * SIA-O1: gráfico de barras apiladas con la cantidad de trámites por estado en cada convenio.
 * Se dibuja con Java2D, sin librerías externas.
 */
public class GraficoBarras extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color[] COLORES = {
        new Color(240, 173, 78),   // en proceso
        new Color(66, 139, 202),   // completo
        new Color(76, 174, 76),    // aprobado
        new Color(217, 83, 79)     // rechazado
    };

    private Control control;

    /**
     * @param control controlador del que se leen los datos
     */
    public GraficoBarras(Control control) {
        this.control = control;
        setPreferredSize(new Dimension(380, 300));
        setBackground(Color.WHITE);
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<Convenio> convenios = control.listarConvenios();
        EstadoTramite[] estados = EstadoTramite.values();
        int ancho = getWidth();
        int alto = getHeight();
        int margenIzq = 40;
        int margenDer = 16;
        int margenSup = 34;
        int margenInf = 70;

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g2.drawString("Trámites por estado en cada convenio", margenIzq, 20);

        if (convenios.isEmpty()) {
            g2.setFont(getFont());
            g2.drawString("No hay convenios registrados.", margenIzq, alto / 2);
            g2.dispose();
            return;
        }

        int maximo = 1;
        for (Convenio c : convenios) {
            maximo = Math.max(maximo, c.getTramites().size());
        }
        int altoGrafico = alto - margenSup - margenInf;
        int anchoGrafico = ancho - margenIzq - margenDer;

        // eje y con líneas guía
        g2.setFont(getFont().deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        for (int v = 0; v <= maximo; v++) {
            int y = margenSup + altoGrafico - v * altoGrafico / maximo;
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(margenIzq, y, ancho - margenDer, y);
            g2.setColor(Color.GRAY);
            String texto = String.valueOf(v);
            g2.drawString(texto, margenIzq - 8 - fm.stringWidth(texto), y + 4);
        }

        // barras apiladas
        int espacio = anchoGrafico / convenios.size();
        int anchoBarra = Math.max(12, Math.min(60, espacio - 18));
        for (int i = 0; i < convenios.size(); i++) {
            Convenio c = convenios.get(i);
            int x = margenIzq + i * espacio + (espacio - anchoBarra) / 2;
            int base = margenSup + altoGrafico;
            for (int s = 0; s < estados.length; s++) {
                int cantidad = c.contarTramites(estados[s]);
                int h = cantidad * altoGrafico / maximo;
                if (h > 0) {
                    g2.setColor(COLORES[s]);
                    g2.fillRect(x, base - h, anchoBarra, h);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRect(x, base - h, anchoBarra, h);
                    base -= h;
                }
            }
            g2.setColor(Color.DARK_GRAY);
            String etiqueta = c.getIdConvenio().replaceAll("-\\d{4}$", "");
            while (fm.stringWidth(etiqueta) > espacio - 4 && etiqueta.length() > 3) {
                etiqueta = etiqueta.substring(0, etiqueta.length() - 1);
            }
            g2.drawString(etiqueta, x + anchoBarra / 2 - fm.stringWidth(etiqueta) / 2,
                    margenSup + altoGrafico + 16);
        }

        // leyenda
        int xLeyenda = margenIzq;
        int yLeyenda = alto - 22;
        for (int s = 0; s < estados.length; s++) {
            g2.setColor(COLORES[s]);
            g2.fillRect(xLeyenda, yLeyenda - 10, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(estados[s].getEtiqueta(), xLeyenda + 16, yLeyenda);
            xLeyenda += 24 + fm.stringWidth(estados[s].getEtiqueta());
        }
        g2.dispose();
    }
}
