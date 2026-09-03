package com.mycompany.mavenproject1;
import javax.swing.*;
import java.awt.*;

public class PanelEstudiantes extends JPanel 
{

    private Control herramientas;

    public PanelEstudiantes(Control herramientas) {
        this.herramientas = herramientas;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título
        JLabel titulo = new JLabel("Registrar Nuevo Estudiante");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titulo, gbc);
        
        // Separador
        gbc.gridy = 1;
        add(new JSeparator(), gbc);
        
        // Campos del formulario
        gbc.gridwidth = 1;
        
        // RUT
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("RUT:"), gbc);
        
        JTextField txtRut = new JTextField(20);
        gbc.gridx = 1;
        add(txtRut, gbc);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Nombre:"), gbc);
        
        JTextField txtNombre = new JTextField(20);
        gbc.gridx = 1;
        add(txtNombre, gbc);
        
        // Carrera
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Carrera:"), gbc);
        
        JTextField txtCarrera = new JTextField(20);
        gbc.gridx = 1;
        add(txtCarrera, gbc);
        
        // Año de ingreso
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Año de Ingreso:"), gbc);
        
        JSpinner spinnerAnio = new JSpinner(new SpinnerNumberModel(2024, 2000, 2030, 1));
        gbc.gridx = 1;
        add(spinnerAnio, gbc);
        
        // Botón registrar
        JButton btnRegistrar = new JButton("Registrar Estudiante");
        btnRegistrar.setBackground(new Color(0, 123, 255));
        btnRegistrar.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        add(btnRegistrar, gbc);
        
        // Área de mensajes
        JTextArea areaMensajes = new JTextArea(3, 40);
        areaMensajes.setEditable(false);
        areaMensajes.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane scrollMensajes = new JScrollPane(areaMensajes);
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(scrollMensajes, gbc);
        
        // Acción del botón
        btnRegistrar.addActionListener(e -> {
            String rut = txtRut.getText().trim();
            String nombre = txtNombre.getText().trim();
            String carrera = txtCarrera.getText().trim();
            int anio = (int) spinnerAnio.getValue();
            
            if (rut.isEmpty() || nombre.isEmpty() || carrera.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Por favor complete todos los campos", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //Validar formato RUT
            if (!validarFormatoRUT(rut)) 
            {
                JOptionPane.showMessageDialog(this,
                    "Formato de RUT inválido. Use formato: XX.XXX.XXX-X",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            //Validar nombre (solo letras y espacios)
            if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) 
            {
                JOptionPane.showMessageDialog(this,
                    "El nombre solo puede contener letras y espacios",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            //Validar año razonable
            int anioActual = java.time.Year.now().getValue();
            if (anio < 1950 || anio > anioActual + 1) 
            {
                JOptionPane.showMessageDialog(this,
                    "Año de ingreso inválido (debe estar entre 1950 y " + (anioActual + 1) + ")",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (herramientas.buscarEstudiante(rut) != null) {
                JOptionPane.showMessageDialog(this, 
                    "Ya existe un estudiante con ese RUT", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            herramientas.registrarEstudiante(rut, nombre, carrera, anio);
            areaMensajes.setText("Estudiante registrado exitosamente:\n" +
                               "RUT: " + rut + "\n" +
                               "Nombre: " + nombre);
            
            // Limpiar campos
            txtRut.setText("");
            txtNombre.setText("");
            txtCarrera.setText("");
            spinnerAnio.setValue(2024);
            
            JOptionPane.showMessageDialog(this, 
                "Estudiante registrado con éxito", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
        
    private boolean validarFormatoRUT(String rut) {
        if (rut == null || rut.isBlank()) return false;

        String patron = "^\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]$";

        if (!rut.matches(patron)) {
            return false;
        }

        String rutLimpio = rut.replaceAll("[^\\dkK]", "");

        if (rutLimpio.length() < 2) return false;

        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        char dv = rutLimpio.charAt(rutLimpio.length() - 1);

        try {
            int suma = 0;
            int multiplicador = 2;

            for (int i = cuerpo.length() - 1; i >= 0; i--) {
                suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
                multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
            }

            int resto = 11 - (suma % 11);

            char dvCalculado;

            if (resto == 11) {
                dvCalculado = '0';
            } else if (resto == 10) {
                dvCalculado = 'k';
            } else {
                dvCalculado = (char) ('0' + resto);
            }

            return Character.toLowerCase(dv) == Character.toLowerCase(dvCalculado);

        } catch (Exception e) {
            return false;
        }
    }
}