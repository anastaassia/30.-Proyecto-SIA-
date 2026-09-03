package com.mycompany.mavenproject1;

import javax.swing.*;
import java.awt.*;

/**
 * Panel que permite subir, eliminar y visualizar el estado de documentos 
 * asociados a un trámite de intercambio.
 */
public class PanelDocumentos extends JPanel {

    private Control herramientas; // Clase controladora para acceso a datos
    private JTabbedPane tabbedPane; // Referencia al contenedor de pestañas

    public PanelDocumentos(Control herramientas, JTabbedPane tabbedPane) {
        this.herramientas = herramientas;
        this.tabbedPane = tabbedPane;
        initComponents();
    }

    private void initComponents() {
        // Uso de GridBagLayout para un control preciso de la posición de los componentes
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaciado entre componentes
        gbc.anchor = GridBagConstraints.WEST; // Alineación a la izquierda

        // --- TÍTULO ---
        JLabel titulo = new JLabel("Subir Documentos a Trámite");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titulo, gbc);

        // --- SELECTOR DE CONVENIO ---
        gbc.gridwidth = 1; gbc.gridy = 1;
        add(new JLabel("Convenio:"), gbc);

        JComboBox<String> comboConvenioDoc = new JComboBox<>();
        gbc.gridx = 1;
        add(comboConvenioDoc, gbc);

        // --- SELECTOR DE TRÁMITE ---
        // Se llena dinámicamente según el convenio seleccionado
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("ID Trámite:"), gbc);

        JComboBox<String> comboTramite = new JComboBox<>();
        gbc.gridx = 1;
        add(comboTramite, gbc);

        // --- SELECTOR DE TIPO DE DOCUMENTO ---
        // Usa el Enum TipoDocumento (Certificado, Pasaporte, etc.)
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Tipo de Documento:"), gbc);

        JComboBox<TipoDocumento> comboTipoDoc = new JComboBox<>(TipoDocumento.values());
        gbc.gridx = 1;
        add(comboTipoDoc, gbc);

        // --- NOMBRE DEL ARCHIVO ---
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Nombre del Archivo:"), gbc);

        JTextField txtNombreArchivo = new JTextField(20);
        gbc.gridx = 1;
        add(txtNombreArchivo, gbc);

        // Botón para abrir el selector de archivos del sistema
        JButton btnSeleccionar = new JButton("Simular Selección");
        gbc.gridx = 2;
        add(btnSeleccionar, gbc);

        // --- ÁREA DE ESTADO (VISUALIZACIÓN) ---
        JTextArea areaEstado = new JTextArea(8, 40);
        areaEstado.setEditable(false);
        areaEstado.setBorder(BorderFactory.createTitledBorder("Estado del Trámite"));
        JScrollPane scrollEstado = new JScrollPane(areaEstado);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH; // Expande el área en ambas direcciones
        add(scrollEstado, gbc);

        // --- BOTONES DE ACCIÓN ---
        // Botón Subir (Color Amarillo/Ámbar)
        JButton btnSubir = new JButton("Subir Documento");
        btnSubir.setBackground(new Color(255, 193, 7));
        btnSubir.setForeground(Color.BLACK);

        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnSubir, gbc);

        // Botón Eliminar (Color Rojo)
        JButton btnEliminarDoc = new JButton("Eliminar Documento");
        btnEliminarDoc.setBackground(new Color(220, 53, 69));
        btnEliminarDoc.setForeground(Color.WHITE);

        gbc.gridx = 1; gbc.gridy = 7;
        add(btnEliminarDoc, gbc);

        // ---------------------------------------------------------
        // LÓGICA DE EVENTOS Y ACTUALIZACIÓN DINÁMICA
        // ---------------------------------------------------------

        // Inicializar datos al cargar el panel
        actualizarComboConvenios(comboConvenioDoc);

        // Al cambiar convenio, actualizar la lista de trámites disponibles
        comboConvenioDoc.addActionListener(e -> {
            actualizarComboTramites(comboConvenioDoc, comboTramite);
        });

        // Al cambiar trámite, mostrar su estado actual en el JTextArea
        comboTramite.addActionListener(e -> {
            actualizarEstadoTramite(comboConvenioDoc, comboTramite, areaEstado);
        });

        // Refrescar convenios cuando el usuario vuelve a esta pestaña
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == this) {
                actualizarComboConvenios(comboConvenioDoc);
            }
        });

        // Acción: Abrir explorador de archivos
        btnSeleccionar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtNombreArchivo.setText(fileChooser.getSelectedFile().getName());
            }
        });

        // Acción: Registrar documento en el sistema
        /*btnSubir.addActionListener(e -> {
            String idConvenioRaw = (String) comboConvenioDoc.getSelectedItem();
            String idTramite = (String) comboTramite.getSelectedItem();
            TipoDocumento tipo = (TipoDocumento) comboTipoDoc.getSelectedItem();
            String nombreArchivo = txtNombreArchivo.getText().trim();

            if (idConvenioRaw == null || idTramite == null || nombreArchivo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Buscar el objeto convenio real extrayendo el ID (antes del guion)
            Convenio convenio = herramientas.buscarConvenio(idConvenioRaw.split(" - ")[0]);
            if (convenio == null) return;

            // Buscar el trámite dentro del convenio
            Tramite tramite = convenio.getTramites().stream()
                .filter(t -> t.getIdTramite().equals(idTramite))
                .findFirst().orElse(null);

            if (tramite != null) {
                tramite.subirDocumento(tipo, nombreArchivo); // Lógica interna del trámite
                convenio.validarYActualizarEstado(tramite);  // Lógica de negocio: ¿Trámite completo?
                
                actualizarEstadoTramite(comboConvenioDoc, comboTramite, areaEstado);
                JOptionPane.showMessageDialog(this, "Documento subido exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtNombreArchivo.setText("");
            }
        });*/
        // Dentro de initComponents(), después de crear los componentes...

        btnSubir.addActionListener(e -> {
            String idConvenioRaw = (String) comboConvenioDoc.getSelectedItem();
            String idTramite = (String) comboTramite.getSelectedItem();
            TipoDocumento tipo = (TipoDocumento) comboTipoDoc.getSelectedItem();
            String nombreArchivo = txtNombreArchivo.getText().trim();

            if (idConvenioRaw == null || idTramite == null || nombreArchivo.isEmpty()) {
                JOptionPane.showMessageDialog(PanelDocumentos.this,
                    "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String idReal = idConvenioRaw.split(" - ")[0];

            try {
                // Este método lanza TramiteNoEncontradoException y DocumentoDuplicadoException
                herramientas.subirDocumentoATramiteStrict(idReal, idTramite, tipo, nombreArchivo);

                // Actualizar vista
                actualizarEstadoTramite(comboConvenioDoc, comboTramite, areaEstado);
                JOptionPane.showMessageDialog(PanelDocumentos.this,
                    "Documento subido exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtNombreArchivo.setText("");

            } catch (TramiteNoEncontradoException ex) {
                JOptionPane.showMessageDialog(PanelDocumentos.this,
                    "Error: " + ex.getMessage(),
                    "Trámite no encontrado",
                    JOptionPane.ERROR_MESSAGE);
            } catch (DocumentoDuplicadoException ex) {
                JOptionPane.showMessageDialog(PanelDocumentos.this,
                    "Error: " + ex.getMessage(),
                    "Documento duplicado",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción: Eliminar un tipo de documento del trámite
        btnEliminarDoc.addActionListener(e -> {
            String idConvenioRaw = (String) comboConvenioDoc.getSelectedItem();
            String idTramite = (String) comboTramite.getSelectedItem();
            TipoDocumento tipo = (TipoDocumento) comboTipoDoc.getSelectedItem();

            if (idConvenioRaw == null || idTramite == null || tipo == null) {
                JOptionPane.showMessageDialog(this, "Selecciona convenio, trámite y tipo de documento.");
                return;
            }

            String idReal = idConvenioRaw.split(" - ")[0];
            boolean ok = herramientas.eliminarDocumentoDeTramite(idReal, idTramite, tipo);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Documento eliminado.");
                actualizarEstadoTramite(comboConvenioDoc, comboTramite, areaEstado);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el documento.");
            }
        });
    }

    // =========================================================
    // MÉTODOS AUXILIARES PARA MANTENER LA UI SINCRONIZADA
    // =========================================================

    /** Llena el combo de convenios con la lista actual de la base de datos */
    private void actualizarComboConvenios(JComboBox<String> combo) {
        combo.removeAllItems();
        for (Convenio c : herramientas.getConvenios()) {
            combo.addItem(c.getIdConvenio() + " - " + c.getNombre());
        }
    }

    /** Filtra y muestra solo los trámites que pertenecen al convenio seleccionado */
    private void actualizarComboTramites(JComboBox<String> comboConvenio, JComboBox<String> comboTramite) {
        comboTramite.removeAllItems();
        String seleccion = (String) comboConvenio.getSelectedItem();
        if (seleccion != null) {
            Convenio c = herramientas.buscarConvenio(seleccion.split(" - ")[0]);
            if (c != null) {
                for (Tramite t : c.getTramites()) {
                    comboTramite.addItem(t.getIdTramite());
                }
            }
        }
    }

    /** Muestra la información resumida en el área de texto central */
    private void actualizarEstadoTramite(JComboBox<String> comboC, JComboBox<String> comboT, JTextArea area) {
        String idC = (String) comboC.getSelectedItem();
        String idT = (String) comboT.getSelectedItem();

        if (idC != null && idT != null) {
            Convenio c = herramientas.buscarConvenio(idC.split(" - ")[0]);
            if (c != null) {
                Tramite t = c.getTramites().stream()
                    .filter(tram -> tram.getIdTramite().equals(idT))
                    .findFirst().orElse(null);
                if (t != null) {
                    area.setText("Estado: " + t.getEstado() + "\nDocumentos cargados: " + t.getDocumentos().keySet());
                }
            }
        }
    }
}

