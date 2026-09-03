package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.io.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;


public class Main extends JFrame
{
    private Control herramientas;
    //Para la persistencia de los datos 
    private DataStore dataStore;
    private JTabbedPane tabbedPane;
    
    // Paneles principales
    private JPanel panelEstudiantes;
    private JPanel panelTramites;
    private JPanel panelDocumentos;
    private JPanel panelConvenios;
    private JPanel panelRequisitos;
    private JPanel panelGestionConvenios;
    private JPanel panelGestionEst;
    private JPanel panelGestionTramites;
    
    // Componentes reutilizables
    private JComboBox<String> comboConvenios;
    private JComboBox<String> comboEstudiantes;
    private DefaultTableModel modeloTablaConvenios;
    private DefaultTableModel modeloTablaTramites;
    
    public Main() 
    {
        herramientas = new Control();
        dataStore = new DataStore(herramientas);

        //intentar cargar datos
        boolean datosExistian = false;


        try {
            dataStore.load();
        
            // CRÍTICO: Verificar si realmente hay datos
            if (herramientas.getConvenios().isEmpty() && herramientas.getEstudiantes().isEmpty()) {
                System.out.println("Archivos CSV vacíos - Creando datos iniciales");
                herramientas.datos();
                dataStore.save(); // Guardar inmediatamente
                System.out.println("Datos iniciales guardados");
            } else {
                System.out.println("Datos cargados: " + 
                    herramientas.getConvenios().size() + " convenios, " + 
                    herramientas.getEstudiantes().size() + " estudiantes");
                datosExistian = true;
            }
        } 
        catch (java.io.IOException e) 
        {
            System.out.println("Error al cargar - Creando datos iniciales");
            herramientas.datos();
            try {
                dataStore.save();
                System.out.println("Datos iniciales guardados");
            } catch (java.io.IOException ex) {
                System.err.println("Error al guardar datos iniciales: " + ex.getMessage());
            }
        }


        initComponents();
        actualizarTodosDespuesDeCarga();
        //actualizarCombos();
    }

    /**
    * Actualiza todos los paneles después de cargar datos del disco
    * Debe llamarse después de dataStore.load()
    */
    private void actualizarTodosDespuesDeCarga() 
    {
        System.out.println("Actualizando GUI con datos cargados...");

        // Actualizar panel de convenios
        if (panelConvenios instanceof PanelConvenios) 
        {
            ((PanelConvenios) panelConvenios).actualizarTablas();
        }
        System.out.println("GUI actualizada");
    }
    
    private void abrirCarpetaDatos() {
        try {
            File carpeta = new File("data");
            if (!carpeta.exists()) {
                carpeta.mkdirs(); // crea la carpeta si no existe
            }
            Desktop.getDesktop().open(carpeta);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "No se pudo abrir la carpeta: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initComponents() {

        setTitle("Sistema de Gestión de Intercambio Estudiantil");
        // 1. Esto detiene el cierre automático
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 2. Esto agrega la lógica para que la X funcione y llame a tu método guardarYSalir
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                guardarYSalir(); // Ahora sí llamará al diálogo de confirmación
            }
        });

        setLayout(new BorderLayout());

        // Crear pestañas
        tabbedPane = new JTabbedPane();

        // Crear paneles DESPUÉS de crear tabbedPane
        panelEstudiantes = new PanelEstudiantes(herramientas);

        // Agregamos "tabbedPane" como segundo parámetro
        panelTramites = new PanelTramites(herramientas, tabbedPane);
        panelDocumentos = new PanelDocumentos(herramientas, tabbedPane);
        panelConvenios = new PanelConvenios(herramientas, tabbedPane);
        panelRequisitos = new PanelRequisitos(herramientas, tabbedPane);

        panelGestionConvenios = new PanelGestionConvenios(herramientas, this);
        panelGestionEst = new PanelGestionEstudiantes(herramientas, this);
        panelGestionTramites = new PanelGestionTramites(herramientas, this);



        tabbedPane.addTab("Registrar Estudiante", panelEstudiantes);
        tabbedPane.addTab("Crear Trámite", panelTramites);
        tabbedPane.addTab("Subir Documentos", panelDocumentos);
        tabbedPane.addTab("Ver Convenios y Trámites", panelConvenios);
        tabbedPane.addTab("Configurar Requisitos", panelRequisitos);
        tabbedPane.addTab("Convenios", panelGestionConvenios);
        tabbedPane.addTab("Gestión Estudiantes", panelGestionEst);
        tabbedPane.addTab("Gestión Trámites", panelGestionTramites);
       

        add(tabbedPane, BorderLayout.CENTER);

        // Panel inferior
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.setBorder(BorderFactory.createEtchedBorder());

        JLabel lblInfo = new JLabel("Sistema de Intercambio v1.0");
        panelInfo.add(lblInfo);

        JButton btnGuardar = new JButton("Guardar Datos");
        JButton btnAbrirCarpeta = new JButton("Abrir carpeta de datos");


        btnGuardar.addActionListener(e -> {
            try {
                dataStore.save();

                JOptionPane.showMessageDialog(this,
                    "Datos guardados correctamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (java.io.IOException ex) {

                JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAbrirCarpeta.addActionListener(e -> abrirCarpetaDatos());

        panelInfo.add(btnGuardar);
        panelInfo.add(btnAbrirCarpeta);

        add(panelInfo, BorderLayout.SOUTH);

        setSize(900, 600);
        setLocationRelativeTo(null);
    }
    
    // Métodos auxiliares
    
    public void actualizarCombos() {
        // Este método se llama cuando se necesita actualizar todos los combos
        // Por ejemplo, después de registrar un nuevo estudiante
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
    
    private void actualizarComboTramites(JComboBox<String> comboConvenio, JComboBox<String> comboTramite) {
        comboTramite.removeAllItems();
        String idConvenio = (String) comboConvenio.getSelectedItem();
        if (idConvenio != null) {
            Convenio convenio = herramientas.buscarConvenio(idConvenio.split(" - ")[0]);
            if (convenio != null) {
                for (Tramite t : convenio.getTramites()) {
                    comboTramite.addItem(t.getIdTramite());
                }
            }
        }
    }
    
    private void actualizarEstadoTramite(JComboBox<String> comboConvenio, JComboBox<String> comboTramite, JTextArea areaEstado) {
        String idConvenio = (String) comboConvenio.getSelectedItem();
        String idTramite = (String) comboTramite.getSelectedItem();
        
        if (idConvenio != null && idTramite != null) {
            Convenio convenio = herramientas.buscarConvenio(idConvenio.split(" - ")[0]);
            if (convenio != null) {
                Tramite tramite = convenio.getTramites().stream()
                    .filter(t -> t.getIdTramite().equals(idTramite))
                    .findFirst()
                    .orElse(null);
                
                if (tramite != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Estudiante: ").append(tramite.getEstudiante().getNombre()).append("\n");
                    sb.append("Estado: ").append(tramite.getEstado()).append("\n\n");
                    sb.append("Requisitos del convenio:\n");
                    for (TipoDocumento req : convenio.getRequisitos()) {
                        sb.append("  • ").append(req);
                        if (tramite.getDocumentos().containsKey(req)) {
                            sb.append(" (").append(tramite.getDocumentos().get(req).getNombreArchivo()).append(")");
                        } else {
                            sb.append(" (Pendiente)");
                        }
                        sb.append("\n");
                    }
                    sb.append("\nDocumentos subidos: ").append(tramite.getDocumentos().size());
                    sb.append("\nRequisitos totales: ").append(convenio.getRequisitos().size());
                    
                    areaEstado.setText(sb.toString());
                }
            }
        }
    }
    
 
    
    // Genera un único archivo TXT con 3 secciones: Convenios, Estudiantes, Trámites
    private boolean exportarTodoAUnTexto(File destino) {
        if (destino == null) return false;
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8))) {
            // Sección Convenios
            w.write("=== Convenios ===");
            w.write(System.lineSeparator());
            w.write("ID;Nombre;Universidad;País;Duración;Carrera;RequisitosCount;TramitesCount");
            w.write(System.lineSeparator());
            for (Convenio c : herramientas.getConvenios()) {
                String[] cols = new String[] {
                    safe(c.getIdConvenio()),
                    safe(c.getNombre()),
                    safe(c.getUniversidadSocia()),
                    safe(c.getPais()),
                    safe(c.getDuracion()),
                    safe(c.getCarreraAsociada()),
                    String.valueOf(c.getRequisitos() == null ? 0 : c.getRequisitos().size()),
                    String.valueOf(c.getTramites() == null ? 0 : c.getTramites().size())
                };
                w.write(String.join(";", escapeCols(cols)));
                w.write(System.lineSeparator());
            }
            w.write(System.lineSeparator());

            // Sección Estudiantes
            w.write("=== Estudiantes ===");
            w.write(System.lineSeparator());
            w.write("RUT;Nombre;Carrera;AnioIngreso;ConvenioAsociado");
            w.write(System.lineSeparator());
            for (Estudiante e : herramientas.getEstudiantes()) {
                String convenioNombre = e.getConvenio() == null ? "" : safe(e.getConvenio().getIdConvenio());
                String[] cols = new String[] {
                    safe(e.getRut()),
                    safe(e.getNombre()),
                    safe(e.getCarrera()),
                    String.valueOf(e.getAnioIngreso()),
                    convenioNombre
                };
                w.write(String.join(";", escapeCols(cols)));
                w.write(System.lineSeparator());
            }
            w.write(System.lineSeparator());

            // Sección Trámites (todos los trámites de todos los convenios)
            w.write("=== Trámites ===");
            w.write(System.lineSeparator());
            w.write("IDTramite;ConvenioID;EstudianteRUT;Estado;DocumentosCount;DocumentosDetalle");
            w.write(System.lineSeparator());
            for (Convenio c : herramientas.getConvenios()) {
                for (Tramite t : c.getTramites()) {
                    String docsCount = String.valueOf(t.getDocumentos() == null ? 0 : t.getDocumentos().size());
                    String docsDetalle = "-";
                    if (t.getDocumentos() != null && !t.getDocumentos().isEmpty()) {
                        List<String> parts = new ArrayList<>();
                        for (Map.Entry<TipoDocumento, DocumentoSubido> en : t.getDocumentos().entrySet()) {
                            String nombreArchivo = en.getValue() == null ? "" : en.getValue().getNombreArchivo();
                            parts.add(en.getKey().name() + ":" + safe(nombreArchivo));
                        }
                        docsDetalle = String.join(",", parts);
                    }
                    String[] cols = new String[] {
                        safe(t.getIdTramite()),
                        safe(c.getIdConvenio()),
                        t.getEstudiante() == null ? "" : safe(t.getEstudiante().getRut()),
                        t.getEstado() == null ? "" : t.getEstado().name(),
                        docsCount,
                        docsDetalle
                    };
                    w.write(String.join(";", escapeCols(cols)));
                    w.write(System.lineSeparator());
                }
            }

            w.flush();
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Helpers: sanea nulls y limita caracteres problemáticos
    private String safe(String s) {
        return s == null ? "" : s;
    }
    private String[] escapeCols(String[] cols) {
        String[] out = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            out[i] = cols[i].replace(";", ","); // evita romper el separador
        }
        return out;
    }

        /**
     * Guarda los datos y cierra la aplicación
     */
    private void guardarYSalir() 
    {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Desea guardar los cambios antes de salir?",
            "Confirmar salida",
            JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) 
        {
            try 
            {
                dataStore.save();
                JOptionPane.showMessageDialog(this, 
                    "Datos guardados exitosamente", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            } catch (java.io.IOException ex) 
            {
                JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else if (confirmacion == JOptionPane.NO_OPTION) 
        {
            System.exit(0);
        }
        // Si es CANCEL, no hace nada
    }

    /**
    * Valida el formato del RUT chileno (XX.XXX.XXX-X)
    * @param rut RUT a validar
    * @return true si el formato es válido
    */
    private boolean validarFormatoRUT(String rut) 
    {
        if (rut == null || rut.isBlank()) return false;

        // Patrón: 11.111.111-1 o 11111111-1
        String patron = "^\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]$";
        if (!rut.matches(patron)) {
            return false;
        }

        // Validación del dígito verificador
        String rutLimpio = rut.replaceAll("[^\\dk]", "");
        if (rutLimpio.length() < 2) return false;

        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        char dv = rutLimpio.charAt(rutLimpio.length() - 1);

        try {
            int suma = 0;
            int multiplicador = 2;

            for (int i = cuerpo.length() - 1; i >= 0; i--) {
                suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
                multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
            }

            int resto = 11 - (suma % 11);
            char dvCalculado = resto == 11 ? '0' : resto == 10 ? 'k' : (char) ('0' + resto);

            return Character.toLowerCase(dv) == Character.toLowerCase(dvCalculado);
        } catch (Exception e) {
            return false;
        }
   }

    /**
    * Método público para que otros paneles puedan solicitar la persistencia de datos.
    * Guarda los datos actuales en los archivos CSV.
    */
    public void guardarDatos() {
        try {
            dataStore.save();
            System.out.println("Datos guardados automáticamente");
        } catch (java.io.IOException ex) {
            System.err.println("Error al guardar: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error al guardar: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        try {
            // Configurar el Look and Feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}