package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import com.mycompany.mavenproject1.persistencia.DataStore;
import com.mycompany.mavenproject1.persistencia.ExportadorExcel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Ventana principal. A la izquierda hay una barra de navegación con las cuatro áreas
 * del sistema y arriba un menú desplegable por área con todas sus opciones
 * (agregar, ver listado, editar, eliminar, buscar). Al cerrar, se guardan los datos.
 */
public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;
    static final String ESTUDIANTES = "Estudiantes";
    static final String CONVENIOS = "Convenios";
    static final String TRAMITES = "Trámites";
    static final String REPORTES = "Reportes";

    private Control control;
    private DataStore dataStore;

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<String, PanelBase> paneles = new LinkedHashMap<>();
    private final Map<String, JToggleButton> botonesNavegacion = new LinkedHashMap<>();
    private final JLabel lblEstado = new JLabel();
    private final PanelEstudiantes panelEstudiantes;
    private final PanelConvenios panelConvenios;
    private final PanelTramites panelTramites;

    /**
     * @param control   controlador con los datos ya cargados
     * @param dataStore persistencia, para guardar al salir
     */
    public VentanaPrincipal(Control control, DataStore dataStore) {
        super("Sistema de Gestión de Intercambio Estudiantil");
        this.control = control;
        this.dataStore = dataStore;

        panelEstudiantes = new PanelEstudiantes(control);
        panelConvenios = new PanelConvenios(control);
        panelTramites = new PanelTramites(control);
        paneles.put(ESTUDIANTES, panelEstudiantes);
        paneles.put(CONVENIOS, panelConvenios);
        paneles.put(TRAMITES, panelTramites);
        paneles.put(REPORTES, new PanelReportes(control));
        for (Map.Entry<String, PanelBase> e : paneles.entrySet()) {
            e.getValue().setAlCambiarDatos(this::actualizarBarraEstado);
            contenido.add(e.getValue(), e.getKey());
        }

        setJMenuBar(crearMenu());
        setLayout(new BorderLayout());
        add(crearNavegacion(), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        lblEstado.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        add(lblEstado, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(980, 600));
        setSize(1180, 700);
        setLocationRelativeTo(null);
        mostrar(ESTUDIANTES);
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ------------------------------------------------------------ construcción

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        archivo.add(item("Guardar datos ahora", e -> guardarAhora()));
        archivo.add(item("Exportar planilla Excel...", e -> exportarExcel()));
        archivo.add(item("Restaurar datos de ejemplo", e -> restaurarDatosEjemplo()));
        archivo.addSeparator();
        archivo.add(item("Guardar y salir", e -> salir()));
        barra.add(archivo);

        JMenu estudiantes = new JMenu(ESTUDIANTES);
        estudiantes.add(item("Ver listado", e -> mostrar(ESTUDIANTES)));
        estudiantes.add(item("Agregar estudiante...", e -> { mostrar(ESTUDIANTES); panelEstudiantes.agregar(); }));
        estudiantes.add(item("Editar seleccionado...", e -> { mostrar(ESTUDIANTES); panelEstudiantes.editarSeleccionado(); }));
        estudiantes.add(item("Eliminar seleccionado", e -> { mostrar(ESTUDIANTES); panelEstudiantes.eliminarSeleccionado(); }));
        estudiantes.add(item("Buscar", e -> { mostrar(ESTUDIANTES); panelEstudiantes.enfocarBusqueda(); }));
        barra.add(estudiantes);

        JMenu convenios = new JMenu(CONVENIOS);
        convenios.add(item("Ver listado", e -> mostrar(CONVENIOS)));
        convenios.add(item("Agregar convenio...", e -> { mostrar(CONVENIOS); panelConvenios.agregar(); }));
        convenios.add(item("Editar seleccionado (y requisitos)...", e -> { mostrar(CONVENIOS); panelConvenios.editarSeleccionado(); }));
        convenios.add(item("Eliminar seleccionado", e -> { mostrar(CONVENIOS); panelConvenios.eliminarSeleccionado(); }));
        convenios.add(item("Buscar", e -> { mostrar(CONVENIOS); panelConvenios.enfocarBusqueda(); }));
        barra.add(convenios);

        JMenu tramites = new JMenu(TRAMITES);
        tramites.add(item("Ver listado", e -> mostrar(TRAMITES)));
        tramites.add(item("Nuevo trámite...", e -> { mostrar(TRAMITES); panelTramites.crear(); }));
        tramites.add(item("Cambiar estado del seleccionado...", e -> { mostrar(TRAMITES); panelTramites.editarSeleccionado(); }));
        tramites.add(item("Eliminar seleccionado", e -> { mostrar(TRAMITES); panelTramites.eliminarSeleccionado(); }));
        tramites.add(item("Buscar", e -> { mostrar(TRAMITES); panelTramites.enfocarBusqueda(); }));
        tramites.addSeparator();
        tramites.add(item("Subir documento al seleccionado...", e -> { mostrar(TRAMITES); panelTramites.subirDocumento(); }));
        tramites.add(item("Quitar documento del seleccionado...", e -> { mostrar(TRAMITES); panelTramites.quitarDocumento(); }));
        barra.add(tramites);

        JMenu reportes = new JMenu(REPORTES);
        reportes.add(item("Reportes de seguimiento", e -> mostrar(REPORTES)));
        barra.add(reportes);
        return barra;
    }

    private static JMenuItem item(String texto, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(accion);
        return item;
    }

    private JPanel crearNavegacion() {
        JPanel lista = new JPanel(new GridLayout(0, 1, 0, 6));
        lista.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (String seccion : paneles.keySet()) {
            JToggleButton boton = new JToggleButton(seccion);
            boton.setFont(boton.getFont().deriveFont(Font.BOLD, 13f));
            boton.setFocusPainted(false);
            boton.addActionListener(e -> mostrar(seccion));
            grupo.add(boton);
            botonesNavegacion.put(seccion, boton);
            lista.add(boton);
        }
        JPanel navegacion = new JPanel(new BorderLayout());
        navegacion.setBackground(new Color(232, 236, 241));
        navegacion.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));
        navegacion.setPreferredSize(new Dimension(150, 10));
        navegacion.add(lista, BorderLayout.NORTH);
        return navegacion;
    }

    // ------------------------------------------------------------ navegación

    /**
     * Muestra un área del sistema y recarga sus datos.
     *
     * @param seccion nombre del área (Estudiantes, Convenios, Trámites o Reportes)
     */
    public final void mostrar(String seccion) {
        paneles.get(seccion).actualizar();
        tarjetas.show(contenido, seccion);
        botonesNavegacion.get(seccion).setSelected(true);
        actualizarBarraEstado();
    }

    private void actualizarBarraEstado() {
        lblEstado.setText(control.getEstudiantes().size() + " estudiantes  ·  " + control.getConvenios().size()
                + " convenios  ·  " + control.listarTramites().size() + " trámites        Datos en: "
                + dataStore.getCarpeta().toAbsolutePath());
    }

    // ------------------------------------------------------------ archivo

    /** Muestra las líneas de los CSV que no se pudieron cargar, si las hubo. */
    public void mostrarAdvertenciasDeCarga() {
        List<String> advertencias = dataStore.getAdvertencias();
        if (advertencias.isEmpty()) {
            return;
        }
        JTextArea texto = new JTextArea(String.join("\n", advertencias), 10, 70);
        texto.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(texto),
                "Algunas líneas de los archivos CSV no se pudieron cargar", JOptionPane.WARNING_MESSAGE);
    }

    private void guardarAhora() {
        try {
            dataStore.guardar();
            JOptionPane.showMessageDialog(this, "Datos guardados en " + dataStore.getCarpeta().toAbsolutePath(),
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron guardar los datos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarExcel() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("intercambio.xlsx"));
        selector.setFileFilter(new FileNameExtensionFilter("Planilla Excel (*.xlsx)", "xlsx"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".xlsx")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".xlsx");
        }
        try {
            new ExportadorExcel(control).exportar(destino.toPath());
            JOptionPane.showMessageDialog(this, "Planilla creada en " + destino.getAbsolutePath(),
                    "Exportación lista", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo crear la planilla: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurarDatosEjemplo() {
        int r = JOptionPane.showConfirmDialog(this,
                "Se reemplazarán TODOS los datos actuales por los datos de ejemplo.\n¿Continuar?",
                "Restaurar datos de ejemplo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            control.cargarDatosIniciales();
            mostrar(ESTUDIANTES);
        } catch (DatoInvalidoException | RegistroDuplicadoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Guarda los datos (persistencia batch) y cierra la aplicación. */
    private void salir() {
        try {
            dataStore.guardar();
            dispose();
            System.exit(0);
        } catch (IOException ex) {
            int r = JOptionPane.showConfirmDialog(this, "No se pudieron guardar los datos: " + ex.getMessage()
                    + "\n¿Salir de todas formas? Se perderán los cambios.", "Error al guardar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
    }
}
