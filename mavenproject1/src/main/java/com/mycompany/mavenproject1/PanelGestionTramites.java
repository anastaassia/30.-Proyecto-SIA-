package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel para la gestión (ver, editar, eliminar) de trámites de postulación.
 */
public class PanelGestionTramites extends JPanel {

    private Control herramientas;
    private Main main;

    private DefaultTableModel modeloTabla;
    private JTable tablaTramites;
    private JTextField txtBuscar;
    private JComboBox<String> comboFiltroEstado;
    private JButton btnEditar, btnEliminar, btnRefrescar;

    public PanelGestionTramites(Control herramientas, Main main) {
        this.herramientas = herramientas;
        this.main = main;
        initComponents();
        cargarTramites();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: búsqueda y filtros
        JPanel panelSuperior = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panelSuperior.add(new JLabel("Buscar:"), gbc);
        txtBuscar = new JTextField(20);
        gbc.gridx = 1;
        panelSuperior.add(txtBuscar, gbc);

        gbc.gridx = 2;
        panelSuperior.add(new JLabel("Filtrar por estado:"), gbc);
        comboFiltroEstado = new JComboBox<>();
        comboFiltroEstado.addItem("Todos");
        for (Tramite.Estado e : Tramite.Estado.values()) {
            comboFiltroEstado.addItem(e.name());
        }
        gbc.gridx = 3;
        panelSuperior.add(comboFiltroEstado, gbc);

        JButton btnBuscar = new JButton("Buscar");
        gbc.gridx = 4;
        panelSuperior.add(btnBuscar, gbc);

        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de trámites
        String[] columnas = {"ID Trámite", "Convenio", "Estudiante (RUT)", "Estudiante", "Estado", "Documentos", "Requisitos"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaTramites = new JTable(modeloTabla);
        tablaTramites.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(tablaTramites);
        add(scroll, BorderLayout.CENTER);

        // Panel inferior: botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEditar = new JButton("Editar Trámite");
        btnEliminar = new JButton("Eliminar Trámite");
        btnRefrescar = new JButton("Refrescar");
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);
        add(panelBotones, BorderLayout.SOUTH);

        // Listeners
        btnBuscar.addActionListener(e -> buscarTramites());
        btnRefrescar.addActionListener(e -> cargarTramites());
        btnEditar.addActionListener(e -> editarTramite());
        btnEliminar.addActionListener(e -> eliminarTramite());

        // Doble clic para editar
        tablaTramites.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarTramite();
                }
            }
        });
    }

    private void cargarTramites() {
        modeloTabla.setRowCount(0);
        for (Convenio c : herramientas.getConvenios()) {
            for (Tramite t : c.getTramites()) {
                agregarFila(t, c);
            }
        }
    }

    private void buscarTramites() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        String estadoFiltro = (String) comboFiltroEstado.getSelectedItem();
        modeloTabla.setRowCount(0);

        for (Convenio c : herramientas.getConvenios()) {
            for (Tramite t : c.getTramites()) {
                boolean coincideTexto = texto.isEmpty() ||
                        t.getIdTramite().toLowerCase().contains(texto) ||
                        (t.getEstudiante() != null && t.getEstudiante().getRut().toLowerCase().contains(texto)) ||
                        (t.getEstudiante() != null && t.getEstudiante().getNombre().toLowerCase().contains(texto));
                boolean coincideEstado = estadoFiltro.equals("Todos") || t.getEstado().name().equals(estadoFiltro);
                if (coincideTexto && coincideEstado) {
                    agregarFila(t, c);
                }
            }
        }
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron trámites", "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void agregarFila(Tramite t, Convenio c) {
        String estudianteRut = t.getEstudiante() == null ? "-" : t.getEstudiante().getRut();
        String estudianteNombre = t.getEstudiante() == null ? "-" : t.getEstudiante().getNombre();
        int docsSubidos = t.getDocumentos().size();
        int requisitos = c.getRequisitos().size();
        modeloTabla.addRow(new Object[]{
                t.getIdTramite(),
                c.getIdConvenio() + " - " + c.getNombre(),
                estudianteRut,
                estudianteNombre,
                t.getEstado().name(),
                docsSubidos,
                requisitos
        });
    }

    private void editarTramite() {
        int fila = tablaTramites.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un trámite", "Editar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idTramite = (String) modeloTabla.getValueAt(fila, 0);
        String idConvenioRaw = (String) modeloTabla.getValueAt(fila, 1);
        String idConvenio = idConvenioRaw.split(" - ")[0];

        Convenio convenio = herramientas.buscarConvenio(idConvenio);
        if (convenio == null) return;

        Tramite tramite = convenio.getTramites().stream()
                .filter(t -> t.getIdTramite().equals(idTramite))
                .findFirst().orElse(null);
        if (tramite == null) return;

        // Diálogo de edición
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Editar Trámite", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Estado actual
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Estado:"), gbc);
        JComboBox<Tramite.Estado> comboEstado = new JComboBox<>(Tramite.Estado.values());
        comboEstado.setSelectedItem(tramite.getEstado());
        gbc.gridx = 1;
        dialog.add(comboEstado, gbc);

        // Estudiante (cambiar asignación)
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Estudiante (RUT):"), gbc);
        JComboBox<String> comboEstudiantes = new JComboBox<>();
        for (Estudiante e : herramientas.getEstudiantes()) {
            comboEstudiantes.addItem(e.getRut() + " - " + e.getNombre());
        }
        if (tramite.getEstudiante() != null) {
            comboEstudiantes.setSelectedItem(tramite.getEstudiante().getRut() + " - " + tramite.getEstudiante().getNombre());
        }
        gbc.gridx = 1;
        dialog.add(comboEstudiantes, gbc);

        JButton btnGuardar = new JButton("Guardar Cambios");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        dialog.add(btnGuardar, gbc);

        btnGuardar.addActionListener(ev -> {
            Tramite.Estado nuevoEstado = (Tramite.Estado) comboEstado.getSelectedItem();
            String seleccion = (String) comboEstudiantes.getSelectedItem();
            String nuevoRut = seleccion.split(" - ")[0];
            boolean ok = herramientas.editarTramite(idConvenio, idTramite, nuevoEstado, nuevoRut);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Trámite actualizado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarTramites();
                dialog.dispose();
                if (main != null) main.guardarDatos(); // persistencia automática
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarTramite() {
        int fila = tablaTramites.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un trámite", "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idTramite = (String) modeloTabla.getValueAt(fila, 0);
        String idConvenioRaw = (String) modeloTabla.getValueAt(fila, 1);
        String idConvenio = idConvenioRaw.split(" - ")[0];

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el trámite " + idTramite + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = herramientas.eliminarTramite(idConvenio, idTramite);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Trámite eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarTramites();
            if (main != null) main.guardarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}