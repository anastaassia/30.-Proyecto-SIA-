package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Clase padre de los paneles de la ventana principal. Entrega el encabezado común
 * y utilidades (tablas, botones, mensajes). Cada panel sobrescribe {@link #actualizar()}
 * para recargar sus datos cuando la ventana lo muestra.
 */
public abstract class PanelBase extends JPanel {

    private static final long serialVersionUID = 1L;

    private Control control;
    private Runnable alCambiarDatos;

    /**
     * @param control   controlador del sistema
     * @param titulo    título del panel
     * @param subtitulo explicación breve de lo que se hace en el panel
     */
    protected PanelBase(Control control, String titulo, String subtitulo) {
        this.control = control;
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setForeground(new Color(90, 90, 90));
        encabezado.add(lblTitulo);
        encabezado.add(lblSubtitulo);
        add(encabezado, BorderLayout.NORTH);
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public Runnable getAlCambiarDatos() {
        return alCambiarDatos;
    }

    /** @param alCambiarDatos acción que se ejecuta cuando el panel modifica datos */
    public void setAlCambiarDatos(Runnable alCambiarDatos) {
        this.alCambiarDatos = alCambiarDatos;
    }

    /** Recarga los datos que muestra el panel. Cada panel la sobrescribe. */
    public abstract void actualizar();

    /** Recarga el panel y avisa a la ventana que los datos cambiaron. */
    protected void notificarCambio() {
        actualizar();
        if (alCambiarDatos != null) {
            alCambiarDatos.run();
        }
    }

    // ------------------------------------------------------------ utilidades de construcción

    /** @return modelo de tabla cuyas celdas no se pueden editar directamente */
    protected static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /** @return tabla de selección simple, ordenable por columna */
    protected static JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    /**
     * @return el valor de la columna 0 (la clave) de la fila seleccionada, o null si no hay selección
     */
    protected static String claveSeleccionada(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return String.valueOf(tabla.getModel().getValueAt(tabla.convertRowIndexToModel(fila), 0));
    }

    /** Vuelve a seleccionar la fila cuya clave (columna 0) es la indicada. */
    protected static void seleccionarClave(JTable tabla, String clave) {
        if (clave == null) {
            return;
        }
        for (int i = 0; i < tabla.getRowCount(); i++) {
            if (clave.equals(String.valueOf(tabla.getValueAt(i, 0)))) {
                tabla.setRowSelectionInterval(i, i);
                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                return;
            }
        }
    }

    protected static JButton crearBoton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        return boton;
    }

    protected static JPanel crearBarra() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barra.setOpaque(false);
        return barra;
    }

    /** @return campo de texto que ejecuta la acción cada vez que cambia su contenido */
    protected static JTextField crearBuscador(Runnable alEscribir) {
        JTextField campo = new JTextField(18);
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alEscribir.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                alEscribir.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                alEscribir.run();
            }
        });
        return campo;
    }

    // ------------------------------------------------------------ mensajes

    protected void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }

    protected void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    protected void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
