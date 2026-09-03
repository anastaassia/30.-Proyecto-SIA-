package com.mycompany.mavenproject1;

import javax.swing.*;
import java.awt.*;

public class PanelRequisitos extends JPanel {

    private Control herramientas;
    private JTabbedPane tabbedPane;

    public PanelRequisitos(Control herramientas, JTabbedPane tabbedPane) {
        this.herramientas = herramientas;
        this.tabbedPane = tabbedPane;

        initComponents();
    }

    private void initComponents() {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Título
        JLabel titulo = new JLabel("Configurar Requisitos de Convenio");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(titulo, gbc);

        // Convenio
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Convenio:"), gbc);

        JComboBox<String> comboConvenioReq = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(comboConvenioReq, gbc);

        // Lista de requisitos actuales
        DefaultListModel<TipoDocumento> modeloRequisitos = new DefaultListModel<>();
        JList<TipoDocumento> listaRequisitos = new JList<>(modeloRequisitos);

        JScrollPane scrollRequisitos = new JScrollPane(listaRequisitos);
        scrollRequisitos.setPreferredSize(new Dimension(250, 150));
        scrollRequisitos.setBorder(BorderFactory.createTitledBorder("Requisitos Actuales"));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollRequisitos, gbc);

        // Lista de tipos disponibles
        DefaultListModel<TipoDocumento> modeloDisponibles = new DefaultListModel<>();

        for (TipoDocumento tipo : TipoDocumento.values()) {
            modeloDisponibles.addElement(tipo);
        }

        JList<TipoDocumento> listaDisponibles = new JList<>(modeloDisponibles);

        JScrollPane scrollDisponibles = new JScrollPane(listaDisponibles);
        scrollDisponibles.setPreferredSize(new Dimension(250, 150));
        scrollDisponibles.setBorder(BorderFactory.createTitledBorder("Tipos Disponibles"));

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(scrollDisponibles, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new GridLayout(2, 1, 5, 5));

        JButton btnAgregar = new JButton("← Agregar");
        JButton btnQuitar = new JButton("Quitar →");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnQuitar);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        add(panelBotones, gbc);

        // Área información
        JTextArea areaInfoReq = new JTextArea(5, 40);
        areaInfoReq.setEditable(false);
        areaInfoReq.setBorder(BorderFactory.createTitledBorder("Información"));

        JScrollPane scrollInfo = new JScrollPane(areaInfoReq);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        add(scrollInfo, gbc);

        // Actualizar combo
        actualizarComboConvenios(comboConvenioReq);

        // Cambio de convenio
        comboConvenioReq.addActionListener(e -> {

            String idConvenio = (String) comboConvenioReq.getSelectedItem();

            if (idConvenio != null) {

                Convenio convenio = herramientas.buscarConvenio(idConvenio.split(" - ")[0]);

                if (convenio != null) {

                    modeloRequisitos.clear();

                    for (TipoDocumento req : convenio.getRequisitos()) {
                        modeloRequisitos.addElement(req);
                    }

                    areaInfoReq.setText(
                        "Convenio: " + convenio.getNombre() + "\n" +
                        "Total de requisitos: " + convenio.getRequisitos().size() + "\n" +
                        "Trámites activos: " + convenio.getTramites().size()
                    );
                }
            }
        });

        // Cambio de pestaña
        tabbedPane.addChangeListener(e -> {

            if (tabbedPane.getSelectedComponent() == this) {
                actualizarComboConvenios(comboConvenioReq);
            }
        });

        // Agregar requisito
        btnAgregar.addActionListener(e -> {

            String idConvenio = (String) comboConvenioReq.getSelectedItem();
            TipoDocumento tipoSeleccionado = listaDisponibles.getSelectedValue();

            if (idConvenio == null || tipoSeleccionado == null) {

                JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un convenio y un tipo de documento",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Convenio convenio = herramientas.buscarConvenio(idConvenio.split(" - ")[0]);

            if (convenio != null) {

                if (!convenio.getRequisitos().contains(tipoSeleccionado)) {

                    convenio.agregarRequisito(tipoSeleccionado);
                    modeloRequisitos.addElement(tipoSeleccionado);

                    areaInfoReq.setText(
                        "Requisito agregado: " + tipoSeleccionado + "\n" +
                        "Total de requisitos: " + convenio.getRequisitos().size() + "\n" +
                        "Trámites revalidados automáticamente"
                    );

                } else {

                    JOptionPane.showMessageDialog(
                        this,
                        "Este requisito ya existe en el convenio",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });

        // Quitar requisito
        btnQuitar.addActionListener(e -> {

            String idConvenio = (String) comboConvenioReq.getSelectedItem();
            TipoDocumento tipoSeleccionado = listaRequisitos.getSelectedValue();

            if (idConvenio == null || tipoSeleccionado == null) {

                JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un convenio y un requisito a quitar",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Convenio convenio = herramientas.buscarConvenio(idConvenio.split(" - ")[0]);

            if (convenio != null) {

                convenio.quitarRequisito(tipoSeleccionado);
                modeloRequisitos.removeElement(tipoSeleccionado);

                areaInfoReq.setText(
                    "Requisito eliminado: " + tipoSeleccionado + "\n" +
                    "Total de requisitos: " + convenio.getRequisitos().size() + "\n" +
                    "Trámites revalidados automáticamente"
                );
            }
        });
    }

    private void actualizarComboConvenios(JComboBox<String> combo) {

        combo.removeAllItems();

        for (Convenio c : herramientas.getConvenios()) {
            combo.addItem(c.getIdConvenio() + " - " + c.getNombre());
        }
    }
}

