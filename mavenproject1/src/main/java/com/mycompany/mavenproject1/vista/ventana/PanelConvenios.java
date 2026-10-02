package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.excepciones.ConvenioNoEncontradoException;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import com.mycompany.mavenproject1.modelo.Tramite;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Panel de convenios: listado, alta, edición (incluidos los requisitos), eliminación y búsqueda.
 * Es el único lugar donde se muestran los datos de los convenios.
 */
public class PanelConvenios extends PanelBase {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modelo = crearModelo("ID", "Nombre", "Universidad", "País", "Meses",
            "Carrera", "Docs. exigidos", "Trámites");
    private final JTable tabla = crearTabla(modelo);
    private final JTextField txtBuscar = crearBuscador(this::actualizar);
    private final JLabel lblTotal = new JLabel();
    private final JTextArea txtDetalle = new JTextArea(5, 40);

    /**
     * @param control controlador del sistema
     */
    public PanelConvenios(Control control) {
        super(control, "Convenios", "Convenios de intercambio y los documentos que exige cada uno. Doble clic para editar.");

        JPanel barra = crearBarra();
        barra.add(crearBoton("Agregar", e -> agregar()));
        barra.add(crearBoton("Editar", e -> editarSeleccionado()));
        barra.add(crearBoton("Eliminar", e -> eliminarSeleccionado()));
        barra.add(new JLabel("     Buscar:"));
        barra.add(txtBuscar);

        int[] anchos = {115, 180, 250, 80, 50, 200, 95, 65};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarSeleccionado();
                }
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });
        txtDetalle.setEditable(false);
        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);
        JScrollPane scrollDetalle = new JScrollPane(txtDetalle);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Convenio seleccionado: requisitos y trámites"));

        JPanel listado = new JPanel(new BorderLayout(0, 4));
        listado.setOpaque(false);
        listado.add(new JScrollPane(tabla), BorderLayout.CENTER);
        listado.add(lblTotal, BorderLayout.SOUTH);
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listado, scrollDetalle);
        division.setResizeWeight(0.65);
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
        String seleccion = claveSeleccionada(tabla);
        modelo.setRowCount(0);
        for (Convenio c : getControl().buscarConvenios(txtBuscar.getText())) {
            modelo.addRow(new Object[]{c.getIdConvenio(), c.getNombre(), c.getUniversidadSocia(), c.getPais(),
                c.getDuracionMeses(), c.getCarreraAsociada(), c.getRequisitos().size(), c.getTramites().size()});
        }
        seleccionarClave(tabla, seleccion);
        lblTotal.setText(modelo.getRowCount() + " de " + getControl().getConvenios().size() + " convenios");
        mostrarDetalle();
    }

    /** Muestra los requisitos completos y la colección anidada de trámites del convenio seleccionado. */
    private void mostrarDetalle() {
        String id = claveSeleccionada(tabla);
        if (id == null) {
            txtDetalle.setText("Seleccione un convenio para ver sus requisitos y trámites.");
            return;
        }
        try {
            Convenio c = getControl().buscarConvenio(id);
            List<String> requisitos = new ArrayList<>();
            for (TipoDocumento td : c.getRequisitos()) {
                requisitos.add(td.getEtiqueta());
            }
            StringBuilder sb = new StringBuilder();
            sb.append(c.getIdConvenio()).append(" · ").append(c.getUniversidadSocia()).append(" (")
              .append(c.getPais()).append(") · ").append(c.getDuracionMeses()).append(" meses\n");
            sb.append("Documentos exigidos: ").append(String.join(", ", requisitos)).append("\n");
            if (c.getTramites().isEmpty()) {
                sb.append("Trámites: ninguno todavía.");
            } else {
                sb.append("Trámites (").append(c.getTramites().size()).append("):\n");
                for (Tramite t : c.getTramites()) {
                    sb.append("   ").append(t.getIdTramite()).append("  ").append(t.getEstudiante().getNombre())
                      .append("  —  ").append(t.getEstado().getEtiqueta()).append("\n");
                }
            }
            txtDetalle.setText(sb.toString());
            txtDetalle.setCaretPosition(0);
        } catch (ConvenioNoEncontradoException ex) {
            txtDetalle.setText("");
        }
    }

    /** Abre el formulario para agregar un convenio. */
    public void agregar() {
        DialogoConvenio dialogo = new DialogoConvenio(SwingUtilities.getWindowAncestor(this), getControl(), null);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            txtBuscar.setText("");
            notificarCambio();
            seleccionarClave(tabla, dialogo.getConvenio().getIdConvenio());
        }
    }

    /** Abre el formulario de edición del convenio seleccionado. */
    public void editarSeleccionado() {
        String id = claveSeleccionada(tabla);
        if (id == null) {
            mostrarAviso("Seleccione un convenio en la tabla.");
            return;
        }
        try {
            Convenio c = getControl().buscarConvenio(id);
            DialogoConvenio dialogo = new DialogoConvenio(SwingUtilities.getWindowAncestor(this), getControl(), c);
            dialogo.setVisible(true);
            if (dialogo.isGuardado()) {
                notificarCambio();
            }
        } catch (ConvenioNoEncontradoException ex) {
            mostrarError(ex);
        }
    }

    /** Elimina el convenio seleccionado (y sus trámites) previa confirmación. */
    public void eliminarSeleccionado() {
        String id = claveSeleccionada(tabla);
        if (id == null) {
            mostrarAviso("Seleccione un convenio en la tabla.");
            return;
        }
        try {
            Convenio c = getControl().buscarConvenio(id);
            if (confirmar("¿Eliminar el convenio " + c + "?\nTambién se eliminarán sus "
                    + c.getTramites().size() + " trámite(s).")) {
                getControl().eliminarConvenio(id);
                notificarCambio();
            }
        } catch (ConvenioNoEncontradoException ex) {
            mostrarError(ex);
        }
    }

    /** Lleva el cursor al campo de búsqueda. */
    public void enfocarBusqueda() {
        txtBuscar.requestFocusInWindow();
        txtBuscar.selectAll();
    }
}
