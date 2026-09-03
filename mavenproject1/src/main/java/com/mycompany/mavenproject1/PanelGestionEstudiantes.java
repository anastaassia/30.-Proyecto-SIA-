package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class PanelGestionEstudiantes extends JPanel
{
    private Control herramientas;
    private Main main;

    public PanelGestionEstudiantes(Control herramientas, Main main)
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
        JLabel titulo = new JLabel("Gestión de Estudiantes");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titulo, gbc);

        gbc.gridwidth = 1;

        // RUT (solo lectura para búsqueda)
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("RUT:"), gbc);
        JTextField txtRut = new JTextField(20);
        gbc.gridx = 1;
        add(txtRut, gbc);

        // Nombre
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(20);
        gbc.gridx = 1;
        add(txtNombre, gbc);

        // Carrera
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Carrera:"), gbc);
        JTextField txtCarrera = new JTextField(20);
        gbc.gridx = 1;
        add(txtCarrera, gbc);

        // Año
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Año Ingreso:"), gbc);
        JSpinner spinnerAnio = new JSpinner(new SpinnerNumberModel(2024, 2000, 2030, 1));
        gbc.gridx = 1;
        add(spinnerAnio, gbc);

        // Estado
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Estado:"), gbc);
        String[] estados = {"Postulación", "Aceptado", "Rechazado", "En curso"};
        JComboBox<String> comboEstado = new JComboBox<>(estados);
        gbc.gridx = 1;
        add(comboEstado, gbc);

        // Botones
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnListar = new JButton("Listar Todos");
        panelBtns.add(btnBuscar);
        panelBtns.add(btnModificar);
        panelBtns.add(btnEliminar);
        panelBtns.add(btnListar);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        add(panelBtns, gbc);

        // Tabla de estudiantes
        String[] cols = {"RUT", "Nombre", "Carrera", "Año", "Estado", "Convenio"};
        DefaultTableModel modeloEst = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaEst = new JTable(modeloEst);
        JScrollPane scrollTabla = new JScrollPane(tablaEst);
        scrollTabla.setPreferredSize(new Dimension(750, 200));

        gbc.gridy = 7; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        add(scrollTabla, gbc);

        // Acción buscar
        btnBuscar.addActionListener(e -> {
            String rut = txtRut.getText().trim();
            if (rut.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese un RUT", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Estudiante est = herramientas.buscarEstudiante(rut);
            if (est == null) {
                JOptionPane.showMessageDialog(this, "Estudiante no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            txtNombre.setText(est.getNombre());
            txtCarrera.setText(est.getCarrera());
            spinnerAnio.setValue(est.getAnioIngreso());
            comboEstado.setSelectedItem(est.getEstadoProceso());
        });

        // Acción modificar
        btnModificar.addActionListener(e -> {
            String rut = txtRut.getText().trim();
            if (rut.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el RUT del estudiante", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String nombre = txtNombre.getText().trim();
            String carrera = txtCarrera.getText().trim();
            Integer anio = (Integer) spinnerAnio.getValue();
            String estado = (String) comboEstado.getSelectedItem();

            boolean ok = herramientas.editarEstudiante(rut, nombre, carrera, anio, estado);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Estudiante modificado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                btnListar.doClick(); // Refrescar tabla
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo modificar el estudiante", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción eliminar
        btnEliminar.addActionListener(e -> {
            String rut = txtRut.getText().trim();
            if (rut.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el RUT", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int conf = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar este estudiante?\nSe eliminarán también sus trámites asociados.", 
                "Confirmar", 
                JOptionPane.YES_NO_OPTION);

            if (conf == JOptionPane.YES_OPTION) {
                boolean ok = herramientas.eliminarEstudiante(rut);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Estudiante eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    txtRut.setText(""); txtNombre.setText(""); txtCarrera.setText("");
                    btnListar.doClick();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Acción listar
        btnListar.addActionListener(e -> {
            modeloEst.setRowCount(0);
            for (Estudiante est : herramientas.getEstudiantes()) {
                String conv = est.getConvenio() == null ? "-" : est.getConvenio().getIdConvenio();
                modeloEst.addRow(new Object[]{
                    est.getRut(), est.getNombre(), est.getCarrera(), 
                    est.getAnioIngreso(), est.getEstadoProceso(), conv
                });
            }
        });

        // Doble click en tabla
        tablaEst.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaEst.getSelectedRow();
                    if (fila >= 0) {
                        txtRut.setText((String) modeloEst.getValueAt(fila, 0));
                        btnBuscar.doClick();
                    }
                }
            }
        });

    }
}
