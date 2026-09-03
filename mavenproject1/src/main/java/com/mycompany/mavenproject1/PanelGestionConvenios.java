package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class PanelGestionConvenios extends JPanel
{
    private Control herramientas;
    private Main main;

    public PanelGestionConvenios(Control herramientas, Main main)
    {
        this.herramientas = herramientas;
        this.main = main;

        initComponents();
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Título
        JLabel titulo = new JLabel("Gestión de Convenios");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(titulo, gbc);

        // Separador
        gbc.gridy = 1;
        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(700, 1));
        add(sep, gbc);

        // Etiquetas + campos (cada fila tiene su propio gbc.gridy)
        gbc.gridwidth = 1;

        // ID
        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("ID Convenio:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtId = new JTextField(20);
        add(txtId, gbc);

        // Nombre
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtNombre = new JTextField(20);
        add(txtNombre, gbc);

        // Universidad socia
        gbc.gridy = 4; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Universidad Socia:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtUni = new JTextField(20);
        add(txtUni, gbc);

        // País
        gbc.gridy = 5; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("País:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtPais = new JTextField(20);
        add(txtPais, gbc);

        // Duración
        gbc.gridy = 6; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Duración:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtDur = new JTextField(20);
        add(txtDur, gbc);

        // Carrera asociada
        gbc.gridy = 7; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Carrera Asociada:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtCarr = new JTextField(20);
        add(txtCarr, gbc);

        // Botones (ponerlos en un panel con FlowLayout)
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnAgregar = new JButton("Agregar Convenio");
        JButton btnModificar = new JButton("Modificar Convenio");
        JButton btnEliminar = new JButton("Eliminar Convenio");
        JButton btnListar = new JButton("Listar Convenios");
        panelBtns.add(btnAgregar);
        panelBtns.add(btnModificar);
        panelBtns.add(btnEliminar);
        panelBtns.add(btnListar);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(panelBtns, gbc);

        // Área de resultado (texto) y tabla (lista)
        JTextArea areaResultado = new JTextArea(6, 60);
        areaResultado.setEditable(false);
        areaResultado.setBorder(BorderFactory.createTitledBorder("Resultado / Convenios"));

        JScrollPane scrollRes = new JScrollPane(areaResultado);

        String[] cols = {"ID", "Nombre", "Universidad", "País", "Duración", "Carrera"};
        DefaultTableModel modeloConvenios = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaConvenios = new JTable(modeloConvenios);
        JScrollPane scrollTabla = new JScrollPane(tablaConvenios);
        scrollTabla.setPreferredSize(new Dimension(750, 150));

        // Añadir areaResultado y tabla; dejar que ocupen el espacio restante
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollRes, scrollTabla);
        split.setResizeWeight(0.3);
        add(split, gbc);

        // Restaurar constraints para llamadas posteriores
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;

        // Helper para refrescar lista y tabla
        Runnable refrescarLista = () -> {
            modeloConvenios.setRowCount(0);
            StringBuilder sb = new StringBuilder();
            for (Convenio c : herramientas.getConvenios()) {
                Object[] fila = {
                    c.getIdConvenio(), c.getNombre(), c.getUniversidadSocia(),
                    c.getPais(), c.getDuracion(), c.getCarreraAsociada()
                };
                modeloConvenios.addRow(fila);
                sb.append(c.getIdConvenio()).append(" - ").append(c.getNombre())
                  .append(" | ").append(c.getUniversidadSocia()).append(" | ")
                  .append(c.getPais()).append(" | ").append(c.getDuracion())
                  .append(" | ").append(c.getCarreraAsociada()).append("\n");
            }
            areaResultado.setText(sb.toString());
            // Si tienes otros combos que dependen de convenios, actualízalos aquí:
            try { main.actualizarCombos(); } catch (Exception ex) { /* noop si no existe */ }
        };

        // Acciones

        btnAgregar.addActionListener(e -> {
            String id = txtId.getText().trim();
            String nom = txtNombre.getText().trim();
            String uni = txtUni.getText().trim();
            String pais = txtPais.getText().trim();
            String dur = txtDur.getText().trim();
            String car = txtCarr.getText().trim();

            if (id.isEmpty() || nom.isEmpty() || uni.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID, Nombre y Universidad son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (herramientas.buscarConvenio(id) != null) {
                JOptionPane.showMessageDialog(this, "Ya existe un convenio con ese ID", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Set<TipoDocumento> requisitos = new HashSet<>(); // vacío por ahora
            Convenio c = new Convenio(id, nom, uni, pais, requisitos, dur, car);
            herramientas.agregarConvenio(c);
            JOptionPane.showMessageDialog(this, "Convenio agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // limpiar
            txtId.setText(""); txtNombre.setText(""); txtUni.setText("");
            txtPais.setText(""); txtDur.setText(""); txtCarr.setText("");
            refrescarLista.run();
        });

        btnModificar.addActionListener(e -> {
            String id = txtId.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el ID del convenio a modificar en el campo ID Convenio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Convenio c = herramientas.buscarConvenio(id);
            if (c == null) {
                JOptionPane.showMessageDialog(this, "No existe convenio con id: " + id, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String nom = txtNombre.getText().trim(); if (nom.isBlank()) nom = null;
            String uni = txtUni.getText().trim(); if (uni.isBlank()) uni = null;
            String pais = txtPais.getText().trim(); if (pais.isBlank()) pais = null;
            String dur = txtDur.getText().trim(); if (dur.isBlank()) dur = null;
            String car = txtCarr.getText().trim(); if (car.isBlank()) car = null;

            boolean ok = herramientas.editarConvenio(id, nom, uni, pais, dur, car);
            JOptionPane.showMessageDialog(this, ok ? "Convenio actualizado." : "No se pudo actualizar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            refrescarLista.run();
        });

        btnEliminar.addActionListener(e -> {
            String id = txtId.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el ID del convenio a eliminar en el campo ID Convenio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Convenio c = herramientas.buscarConvenio(id);
            if (c == null) {
                JOptionPane.showMessageDialog(this, "No existe convenio con id: " + id, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int conf = JOptionPane.showConfirmDialog(this, "¿Confirmas eliminar el convenio " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;

            boolean ok = herramientas.eliminarConvenio(id);
            JOptionPane.showMessageDialog(this, ok ? "Convenio eliminado." : "No se pudo eliminar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            refrescarLista.run();
        });

        btnListar.addActionListener(e -> refrescarLista.run());

        // Doble click en tabla llena campos para editar/eliminar
        tablaConvenios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaConvenios.getSelectedRow();
                    if (fila >= 0) {
                        txtId.setText((String) modeloConvenios.getValueAt(fila, 0));
                        txtNombre.setText((String) modeloConvenios.getValueAt(fila, 1));
                        txtUni.setText((String) modeloConvenios.getValueAt(fila, 2));
                        txtPais.setText((String) modeloConvenios.getValueAt(fila, 3));
                        txtDur.setText((String) modeloConvenios.getValueAt(fila, 4));
                        txtCarr.setText((String) modeloConvenios.getValueAt(fila, 5));
                    }
                }
            }
        });

        // Inicializar contenido (en el EDT)
        SwingUtilities.invokeLater(refrescarLista);

    }
}
 