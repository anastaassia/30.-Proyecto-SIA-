package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

/**
 * Panel para la visualización, búsqueda y gestión de convenios y trámites.
 * Permite filtrar información por estudiante, convenio o estado del trámite.
 */
public class PanelConvenios extends JPanel {

    private Control herramientas; // Clase controladora para acceder a los datos
    private JTabbedPane tabbedPane; // Referencia al contenedor de pestañas principal
    private DefaultTableModel modeloTablaConvenios; // Modelo de datos para la tabla superior
    private DefaultTableModel modeloTablaTramites;  // Modelo de datos para la tabla inferior

    public PanelConvenios(Control herramientas, JTabbedPane tabbedPane) {
        this.herramientas = herramientas;
        this.tabbedPane = tabbedPane;
        initComponents();
    }

    private void initComponents() {
        // Configuración básica del panel principal
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- SECCIÓN NORTE: Título y Botones de acción rápida ---
        JPanel panelNorth = new JPanel(new BorderLayout());
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titulo = new JLabel("Listado de Convenios y Trámites");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panelTitulo.add(titulo);

        // Botón para refrescar manualmente las tablas
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarTablas());
        panelTitulo.add(btnActualizar);

        // Botón para exportar la base de datos a un archivo de texto
        JButton btnExportAll = new JButton("Exportar Todo (.txt)");
        btnExportAll.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Exportar datos a archivo TXT");
            fc.setSelectedFile(new File("export_all.txt"));
            int opt = fc.showSaveDialog(this);
            if (opt == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                // Aquí iría la lógica de escritura en archivo
            }
        });
        panelTitulo.add(btnExportAll);

        panelNorth.add(panelTitulo, BorderLayout.NORTH);

        // --- SECCIÓN DE BÚSQUEDA Y FILTROS ---
        JPanel panelBuscar = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Selector de modo de búsqueda
        gbc.gridx = 0; gbc.gridy = 0;
        panelBuscar.add(new JLabel("Buscar en:"), gbc);

        String[] opciones = {"Estudiantes por nombre", "Convenios por ID", "Trámites por ID/Estado"};
        JComboBox<String> comboModo = new JComboBox<>(opciones);
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelBuscar.add(comboModo, gbc);

        // Campo de texto para ingresar el criterio de búsqueda
        JTextField txtBuscar = new JTextField();
        txtBuscar.setToolTipText("Texto a buscar. Ej: 'Bruno', 'A-2025', 'T-1' o 'EN_PROCESO'");
        gbc.gridx = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelBuscar.add(txtBuscar, gbc);

        JButton btnBuscar = new JButton("Buscar");
        gbc.gridx = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panelBuscar.add(btnBuscar, gbc);

        // Filtro adicional por estado (específico para trámites)
        gbc.gridx = 0; gbc.gridy = 1;
        panelBuscar.add(new JLabel("Filtro estado (trámites):"), gbc);
        JComboBox<String> comboEstado = new JComboBox<>();
        comboEstado.addItem("Todos");
        for (Tramite.Estado s : Tramite.Estado.values()) comboEstado.addItem(s.name());
        gbc.gridx = 1; gbc.gridwidth = 1;
        panelBuscar.add(comboEstado, gbc);

        panelNorth.add(panelBuscar, BorderLayout.SOUTH);
        add(panelNorth, BorderLayout.NORTH);

        // --- SECCIÓN CENTRAL: Tablas con SplitPane ---
        // Permite dividir el espacio verticalmente entre las dos tablas
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5); // 50% de espacio para cada tabla inicial

        // Configuración de Tabla de Convenios (Superior)
        String[] columnasConvenios = {"ID", "Nombre", "Universidad", "País", "Duración", "Carrera"};
        modeloTablaConvenios = new DefaultTableModel(columnasConvenios, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaConvenios = new JTable(modeloTablaConvenios);
        JScrollPane scrollConvenios = new JScrollPane(tablaConvenios);
        scrollConvenios.setBorder(BorderFactory.createTitledBorder("Convenios"));

        // Configuración de Tabla de Trámites (Inferior)
        String[] columnasTramites = {"ID Trámite", "Convenio", "Estudiante", "Estado", "Documentos"};
        modeloTablaTramites = new DefaultTableModel(columnasTramites, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tablaTramites = new JTable(modeloTablaTramites);
        JScrollPane scrollTramites = new JScrollPane(tablaTramites);
        scrollTramites.setBorder(BorderFactory.createTitledBorder("Trámites"));

        splitPane.setTopComponent(scrollConvenios);
        splitPane.setBottomComponent(scrollTramites);
        add(splitPane, BorderLayout.CENTER);

        // --- SECCIÓN SUR: Detalles ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDetalles = new JButton("Ver Detalles del Trámite");
        btnDetalles.addActionListener(e -> {
            int fila = tablaTramites.getSelectedRow();
            if (fila >= 0) {
                String idTramite = (String) modeloTablaTramites.getValueAt(fila, 0);
                String idConvenio = (String) modeloTablaTramites.getValueAt(fila, 1);
                mostrarDetallesTramite(idConvenio, idTramite);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un trámite de la tabla inferior", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelBotones.add(btnDetalles);
        add(panelBotones, BorderLayout.SOUTH);

        // --- LÓGICA DE BÚSQUEDA INTEGRADA ---
        btnBuscar.addActionListener(e -> {
            String modo = (String) comboModo.getSelectedItem();
            String q = txtBuscar.getText().trim();
            
            // Limpiamos resultados previos
            modeloTablaConvenios.setRowCount(0);
            modeloTablaTramites.setRowCount(0);

            try {
                if ("Estudiantes por nombre".equals(modo)) {
                    // Búsqueda de estudiantes y sus trámites asociados
                    if (q.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Ingrese un nombre", "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    java.util.List<Estudiante> encontrados = herramientas.buscarEstudiantesPorNombre(q);
                    if (encontrados == null || encontrados.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No se encontró ningún estudiante con ese nombre", "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // Usamos un Set para no repetir convenios si un estudiante tiene varios trámites en el mismo
                    LinkedHashSet<Convenio> conveniosRelacionados = new LinkedHashSet<>();
                    java.util.List<Tramite> tramitesEncontrados = new ArrayList<>();

                    for (Estudiante s : encontrados) {
                        for (Convenio c : herramientas.getConvenios()) {
                            for (Tramite t : c.getTramites()) {
                                if (t.getEstudiante() != null && t.getEstudiante().getRut().equals(s.getRut())) {
                                    conveniosRelacionados.add(c);
                                    tramitesEncontrados.add(t);
                                }
                            }
                        }
                    }

                    // Llenar tabla superior con convenios donde el estudiante participa
                    for (Convenio c : conveniosRelacionados) {
                        modeloTablaConvenios.addRow(new Object[]{
                            c.getIdConvenio(), c.getNombre(), c.getUniversidadSocia(),
                            c.getPais(), c.getDuracion(), c.getCarreraAsociada()
                        });
                    }

                    // Llenar tabla inferior con los trámites específicos de esos estudiantes
                    for (Tramite t : tramitesEncontrados) {
                        String docs = (t.getDocumentos() == null || t.getDocumentos().isEmpty()) ? "0" : String.valueOf(t.getDocumentos().size());
                        String convenioId = "-";
                        for (Convenio c : herramientas.getConvenios()) {
                            if (c.getTramites().contains(t)) { convenioId = c.getIdConvenio(); break; }
                        }
                        modeloTablaTramites.addRow(new Object[]{t.getIdTramite(), convenioId, t.getEstudiante().getRut(), t.getEstado().name(), docs});
                    }

                } else if ("Convenios por ID".equals(modo)) {
                    // Lógica para filtrar por identificador de convenio
                    java.util.List<Convenio> encontrados = herramientas.buscarConveniosPorId(q);
                    for (Convenio c : encontrados) {
                        modeloTablaConvenios.addRow(new Object[]{c.getIdConvenio(), c.getNombre(), c.getUniversidadSocia(), c.getPais(), c.getDuracion(), c.getCarreraAsociada()});
                        for (Tramite t : c.getTramites()) {
                            modeloTablaTramites.addRow(new Object[]{t.getIdTramite(), c.getIdConvenio(), t.getEstudiante().getRut(), t.getEstado().name(), t.getDocumentos().size()});
                        }
                    }
                } else { 
                    // Búsqueda por ID de trámite o Estado del mismo
                    java.util.List<Tramite> encontrados = herramientas.buscarTramitesPorTexto(q);
                    for (Tramite t : encontrados) {
                        // Encontrar el convenio padre para mostrarlo en la tabla
                        Convenio padre = null;
                        for (Convenio c : herramientas.getConvenios()) {
                            if (c.getTramites().contains(t)) { padre = c; break; }
                        }
                        if (padre != null) {
                            modeloTablaConvenios.addRow(new Object[]{padre.getIdConvenio(), padre.getNombre(), padre.getUniversidadSocia(), padre.getPais(), padre.getDuracion(), padre.getCarreraAsociada()});
                        }
                        modeloTablaTramites.addRow(new Object[]{t.getIdTramite(), (padre != null ? padre.getIdConvenio() : "-"), t.getEstudiante().getRut(), t.getEstado().name(), t.getDocumentos().size()});
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en búsqueda: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Eventos de doble clic para acceso rápido a información
        tablaConvenios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Doble clic
                    int fila = tablaConvenios.getSelectedRow();
                    if (fila >= 0) {
                        String id = (String) modeloTablaConvenios.getValueAt(fila, 0);
                        Convenio c = herramientas.buscarConvenio(id);
                        if (c != null) JOptionPane.showMessageDialog(PanelConvenios.this, c.toString(), "Detalle Convenio", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    /**
     * Crea y muestra una ventana emergente (JDialog) con el desglose de documentos
     * del trámite seleccionado, comparándolos con los requisitos del convenio.

    private void mostrarDetallesTramite(String idConvenio, String idTramite) {
        Convenio convenio = herramientas.buscarConvenio(idConvenio);
        if (convenio == null) return;
        
        Tramite tramite = convenio.getTramites().stream()
            .filter(t -> t.getIdTramite().equals(idTramite))
            .findFirst().orElse(null);
        
        if (tramite == null) return;
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalles del Trámite", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel panelInfo = new JPanel(new GridBagLayout());
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.anchor = GridBagConstraints.WEST;
        
        // Datos del Estudiante y Trámite
        gbc.gridy = 0; panelInfo.add(new JLabel("<html><b>ID Trámite:</b> " + tramite.getIdTramite() + "</html>"), gbc);
        gbc.gridy = 1; panelInfo.add(new JLabel("<html><b>Estudiante:</b> " + tramite.getEstudiante().getNombre() + "</html>"), gbc);
        gbc.gridy = 2; panelInfo.add(new JLabel("<html><b>Convenio:</b> " + convenio.getNombre() + "</html>"), gbc);
        
        // Tabla de requisitos vs documentos subidos
        String[] columnas = {"Requisito", "Estado", "Archivo"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        for (TipoDocumento req : convenio.getRequisitos()) {
            boolean subido = tramite.getDocumentos().containsKey(req);
            modelo.addRow(new Object[]{
                req.toString(), 
                subido ? "✓ RECIBIDO" : "✗ PENDIENTE", 
                subido ? tramite.getDocumentos().get(req).getNombreArchivo() : "---"
            });
        }
        
        JTable tablaReq = new JTable(modelo);
        panelInfo.add(new JScrollPane(tablaReq), gbc); // gbc debe ajustarse para expandir
        
        dialog.add(panelInfo, BorderLayout.CENTER);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());
        dialog.add(btnCerrar, BorderLayout.SOUTH);
        
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }*/
    private void mostrarDetallesTramite(String idConvenio, String idTramite) 
    {
        Convenio convenio = herramientas.buscarConvenio(idConvenio);
        if (convenio == null) return;

        Tramite tramite = convenio.getTramites().stream()
            .filter(t -> t.getIdTramite().equals(idTramite))
            .findFirst().orElse(null);

        if (tramite == null) return;

        // Crear diálogo
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Detalles del Trámite", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel de información superior
        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        panelInfo.setBackground(Color.WHITE);

        JLabel lblIdTramite = new JLabel("ID Trámite: " + tramite.getIdTramite());
        lblIdTramite.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel lblEstudiante = new JLabel("Estudiante: " + tramite.getEstudiante().getNombre());
        lblEstudiante.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel lblRUT = new JLabel("RUT: " + tramite.getEstudiante().getRut());
        lblRUT.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel lblConvenio = new JLabel("Convenio: " + convenio.getNombre());
        lblConvenio.setFont(new Font("Arial", Font.PLAIN, 12));

        panelInfo.add(lblIdTramite);
        panelInfo.add(lblEstudiante);
        panelInfo.add(lblRUT);
        panelInfo.add(lblConvenio);

        // Tabla de documentos
        String[] columnas = {"Tipo Documento", "Estado", "Archivo", "Fecha"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (TipoDocumento req : convenio.getRequisitos()) {
            boolean subido = tramite.getDocumentos().containsKey(req);
            Object[] fila = new Object[4];
            fila[0] = req.toString().replace("_", " ");
            fila[1] = subido ? "✓ Subido" : "✗ Pendiente";
            fila[2] = subido ? tramite.getDocumentos().get(req).getNombreArchivo() : "---";
            fila[3] = subido ? tramite.getDocumentos().get(req).getFechaSubida().toString() : "---";
            modelo.addRow(fila);
        }

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.getTableHeader().setBackground(new Color(70, 130, 180));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Colores alternados
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(240, 248, 255));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }

                // Color especial para estado
                if (column == 1) {
                    if (value.toString().contains("✓")) {
                        setForeground(new Color(0, 128, 0)); // Verde
                    } else {
                        setForeground(new Color(200, 0, 0)); // Rojo
                    }
                } else {
                    setForeground(Color.BLACK);
                }

                return c;
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        // Botón cerrar
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setBackground(Color.WHITE);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setPreferredSize(new Dimension(100, 30));
        btnCerrar.addActionListener(e -> dialog.dispose());
        panelBoton.add(btnCerrar);

        // Ensamblar
        dialog.add(panelInfo, BorderLayout.NORTH);
        dialog.add(scrollTabla, BorderLayout.CENTER);
        dialog.add(panelBoton, BorderLayout.SOUTH);

        dialog.setSize(650, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Limpia y vuelve a cargar todos los datos de la base de datos
     * en las tablas del panel.
     */
    public void actualizarTablas() {
        modeloTablaConvenios.setRowCount(0);
        modeloTablaTramites.setRowCount(0);
        
        for (Convenio c : herramientas.getConvenios()) {
            modeloTablaConvenios.addRow(new Object[]{
                c.getIdConvenio(), c.getNombre(), c.getUniversidadSocia(),
                c.getPais(), c.getDuracion(), c.getCarreraAsociada()
            });
            
            for (Tramite t : c.getTramites()) {
                modeloTablaTramites.addRow(new Object[]{
                    t.getIdTramite(),
                    c.getIdConvenio(),
                    t.getEstudiante().getNombre() + " (" + t.getEstudiante().getRut() + ")",
                    t.getEstado().toString(),
                    t.getDocumentos().size() + "/" + c.getRequisitos().size()
                });
            }
        }
        System.out.println("Tablas actualizadas: " + 
        modeloTablaConvenios.getRowCount() + " convenios, " + 
        modeloTablaTramites.getRowCount() + " trámites");
    }
}