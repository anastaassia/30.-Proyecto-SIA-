package com.mycompany.mavenproject1;

import javax.swing.*;
import java.awt.*;

public class PanelTramites extends JPanel {

    private Control herramientas;
    private JTabbedPane tabbedPane;

    public PanelTramites(Control herramientas, JTabbedPane tabbedPane) {

        this.herramientas = herramientas;
        this.tabbedPane = tabbedPane;

        initComponents();
    }
    
    private void actualizarComboConvenios(JComboBox<String> combo) {
                combo.removeAllItems();

                for (Convenio c : herramientas.getConvenios()) {
                    combo.addItem(c.getIdConvenio() + " - " + c.getNombre());
                }
            }
    private void actualizarComboEstudiantes(JComboBox<String> combo) {
    combo.removeAllItems();

    for (Estudiante e : herramientas.getEstudiantes()) {
        combo.addItem(e.getRut());
        }
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título
        JLabel titulo = new JLabel("Crear Trámite de Postulación");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titulo, gbc);
        
        // Convenio
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Convenio:"), gbc);
        
        JComboBox<String> comboConvenioTramite = new JComboBox<>();
        gbc.gridx = 1;
        add(comboConvenioTramite, gbc);
        
        // Estudiante
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Estudiante (RUT):"), gbc);
        
        JComboBox<String> comboEstudianteTramite = new JComboBox<>();
        gbc.gridx = 1;
        add(comboEstudianteTramite, gbc);
        
        // Campo para ID manual
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("ID Trámite (opcional):"), gbc);

        JTextField txtIdManual = new JTextField(20);
        gbc.gridx = 1;
        add(txtIdManual, gbc);

        // Área de información
        JTextArea areaInfo = new JTextArea(10, 40);
        areaInfo.setEditable(false);
        areaInfo.setBorder(BorderFactory.createTitledBorder("Información del Trámite"));
        JScrollPane scrollInfo = new JScrollPane(areaInfo);
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollInfo, gbc);

        // Botón crear trámite
        JButton btnCrearTramite = new JButton("Crear Trámite");
        btnCrearTramite.setBackground(new Color(40, 167, 69));
        btnCrearTramite.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        add(btnCrearTramite, gbc);

        
        // Actualizar combos
        actualizarComboConvenios(comboConvenioTramite);
        actualizarComboEstudiantes(comboEstudianteTramite);
        
        // Listener para actualizar combos cuando se cambia de pestaña
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == this) {
                actualizarComboConvenios(comboConvenioTramite);
                actualizarComboEstudiantes(comboEstudianteTramite);
            }
        });
        
        // Acción del botón
        btnCrearTramite.addActionListener(e -> {
            String idConvenio = (String) comboConvenioTramite.getSelectedItem();
            String rut = (String) comboEstudianteTramite.getSelectedItem();
            String idManual = txtIdManual.getText().trim();

            if (idConvenio == null || rut == null) {
                JOptionPane.showMessageDialog(this, "Seleccione convenio y estudiante", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Convenio convenio = herramientas.buscarConvenio(idConvenio.split(" - ")[0]);
            Estudiante estudiante = herramientas.buscarEstudiante(rut);

            if (convenio == null || estudiante == null) {
                JOptionPane.showMessageDialog(this, "Datos no válidos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Tramite tramite;

            if (!idManual.isEmpty()) {
                // Validar que no esté duplicado
                boolean existe = convenio.getTramites().stream()
                    .anyMatch(t -> t.getIdTramite().equals(idManual));

                if (existe) {
                    JOptionPane.showMessageDialog(this, "Ya existe un trámite con ese ID", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                tramite = new Tramite(idManual, estudiante, convenio);
                convenio.getTramites().add(tramite);
            } else {
                tramite = convenio.crearTramite(estudiante); // ID automático
            }

            // Mostrar información
            StringBuilder info = new StringBuilder();
            info.append("Trámite creado exitosamente!\n");
            info.append("============================\n");
            info.append("ID Trámite: ").append(tramite.getIdTramite()).append("\n");
            info.append("Estudiante: ").append(estudiante.getNombre()).append("\n");
            info.append("Convenio: ").append(convenio.getNombre()).append("\n");
            info.append("Universidad: ").append(convenio.getUniversidadSocia()).append("\n");
            info.append("País: ").append(convenio.getPais()).append("\n");
            info.append("\nRequisitos necesarios:\n");
            for (TipoDocumento req : convenio.getRequisitos()) {
                info.append(" • ").append(req.toString()).append("\n");
            }
            info.append("\nEstado: ").append(tramite.getEstado());

            areaInfo.setText(info.toString());
            //actualizarTablas();
            JOptionPane.showMessageDialog(this, "Trámite creado con ID: " + tramite.getIdTramite(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        });
    }

}
