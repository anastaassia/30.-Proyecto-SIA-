package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.excepciones.ConvenioNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import com.mycompany.mavenproject1.excepciones.DocumentoDuplicadoException;
import com.mycompany.mavenproject1.excepciones.EstudianteNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import com.mycompany.mavenproject1.excepciones.TramiteNoEncontradoException;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.DocumentoSubido;
import com.mycompany.mavenproject1.modelo.EstadoTramite;
import com.mycompany.mavenproject1.modelo.Estudiante;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import com.mycompany.mavenproject1.modelo.Tramite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel de trámites: listado filtrable por convenio y estado, creación, cambio de estado,
 * eliminación y búsqueda. Abajo muestra los documentos del trámite seleccionado,
 * con las acciones para subir y quitar documentos.
 */
public class PanelTramites extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final String TODOS = "Todos";

    private final DefaultTableModel modelo = crearModelo("ID", "Convenio", "RUT", "Estudiante", "Estado", "Documentos");
    private final JTable tabla = crearTabla(modelo);
    private final JComboBox<Object> cmbConvenio = new JComboBox<>();
    private final JComboBox<Object> cmbEstado = new JComboBox<>();
    private final JTextField txtBuscar = crearBuscador(this::actualizar);
    private final JLabel lblTotal = new JLabel();

    private final DefaultTableModel modeloDocs = crearModelo("Documento exigido", "Estado", "Archivo", "Fecha de subida");
    private final JTable tablaDocs = crearTabla(modeloDocs);
    private final JLabel lblDetalle = new JLabel(" ");
    private final JLabel lblObservacion = new JLabel(" ");
    private boolean actualizandoFiltros;

    /**
     * @param control controlador del sistema
     */
    public PanelTramites(Control control) {
        super(control, "Trámites y documentos",
                "Postulaciones de estudiantes a convenios. Seleccione un trámite para ver y gestionar sus documentos.");

        JPanel acciones = crearBarra();
        acciones.add(crearBoton("Nuevo trámite", e -> crear()));
        acciones.add(crearBoton("Cambiar estado", e -> editarSeleccionado()));
        acciones.add(crearBoton("Eliminar", e -> eliminarSeleccionado()));
        JPanel filtros = crearBarra();
        filtros.add(new JLabel("Filtrar por convenio:"));
        cmbConvenio.setPrototypeDisplayValue("MX-UNAM-2026 - Semestre en la UNAM");
        filtros.add(cmbConvenio);
        filtros.add(new JLabel("  Estado:"));
        filtros.add(cmbEstado);
        filtros.add(new JLabel("  Buscar:"));
        filtros.add(txtBuscar);
        JPanel barra = new JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        barra.setOpaque(false);
        barra.add(acciones);
        barra.add(filtros);
        cmbConvenio.addActionListener(e -> filtrosCambiaron());
        cmbEstado.addActionListener(e -> filtrosCambiaron());

        tabla.getColumnModel().getColumn(3).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new RenderEstado());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });

        JPanel arriba = new JPanel(new BorderLayout(0, 6));
        arriba.setOpaque(false);
        arriba.add(barra, BorderLayout.NORTH);
        arriba.add(new JScrollPane(tabla), BorderLayout.CENTER);
        arriba.add(lblTotal, BorderLayout.SOUTH);

        JPanel barraDocs = crearBarra();
        barraDocs.add(crearBoton("Subir documento", e -> subirDocumento()));
        barraDocs.add(crearBoton("Quitar documento", e -> quitarDocumento()));
        lblObservacion.setForeground(new Color(90, 90, 90));
        tablaDocs.getColumnModel().getColumn(0).setPreferredWidth(220);
        tablaDocs.getColumnModel().getColumn(1).setCellRenderer(new RenderEstado());

        JPanel cabeceraDocs = new JPanel(new BorderLayout());
        cabeceraDocs.setOpaque(false);
        lblDetalle.setFont(lblDetalle.getFont().deriveFont(java.awt.Font.BOLD));
        cabeceraDocs.add(lblDetalle, BorderLayout.NORTH);
        cabeceraDocs.add(lblObservacion, BorderLayout.CENTER);
        cabeceraDocs.add(barraDocs, BorderLayout.SOUTH);

        JPanel abajo = new JPanel(new BorderLayout(0, 4));
        abajo.setBorder(BorderFactory.createTitledBorder("Documentos del trámite seleccionado"));
        abajo.add(cabeceraDocs, BorderLayout.NORTH);
        JScrollPane scrollDocs = new JScrollPane(tablaDocs);
        scrollDocs.setPreferredSize(new Dimension(400, 150));
        abajo.add(scrollDocs, BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        division.setResizeWeight(0.6);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);
        actualizar();
    }

    // ------------------------------------------------------------ listado

    @Override
    public void actualizar() {
        if (actualizandoFiltros) {
            return;
        }
        recargarFiltros();
        String seleccion = claveSeleccionada(tabla);
        modelo.setRowCount(0);
        for (Tramite t : tramitesFiltrados()) {
            modelo.addRow(new Object[]{t.getIdTramite(), t.getConvenio().getIdConvenio(), t.getEstudiante().getRut(),
                t.getEstudiante().getNombre(), t.getEstado(),
                t.getDocumentos().size() + " de " + t.getConvenio().getRequisitos().size()});
        }
        seleccionarClave(tabla, seleccion);
        lblTotal.setText(modelo.getRowCount() + " de " + getControl().listarTramites().size() + " trámites");
        mostrarDetalle();
    }

    /** Aplica los filtros usando las distintas versiones (sobrecarga) de listarTramites. */
    private List<Tramite> tramitesFiltrados() {
        Object convenio = cmbConvenio.getSelectedItem();
        Object estado = cmbEstado.getSelectedItem();
        List<Tramite> base;
        try {
            if (convenio instanceof Convenio) {
                base = getControl().listarTramites(((Convenio) convenio).getIdConvenio());
            } else if (estado instanceof EstadoTramite) {
                base = getControl().listarTramites((EstadoTramite) estado);
            } else {
                base = getControl().listarTramites();
            }
        } catch (ConvenioNoEncontradoException ex) {
            base = getControl().listarTramites();
        }
        List<Tramite> coincidencias = getControl().buscarTramites(txtBuscar.getText());
        List<Tramite> resultado = new ArrayList<>();
        for (Tramite t : base) {
            boolean estadoOk = !(estado instanceof EstadoTramite) || t.getEstado() == estado;
            if (estadoOk && coincidencias.contains(t)) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    /** Vuelve a llenar los combos de filtro (los convenios pueden haber cambiado). */
    private void recargarFiltros() {
        actualizandoFiltros = true;
        Object convenioElegido = cmbConvenio.getSelectedItem();
        Object estadoElegido = cmbEstado.getSelectedItem();
        cmbConvenio.removeAllItems();
        cmbConvenio.addItem(TODOS);
        for (Convenio c : getControl().listarConvenios()) {
            cmbConvenio.addItem(c);
        }
        cmbConvenio.setSelectedItem(convenioElegido == null ? TODOS : convenioElegido);
        if (cmbConvenio.getSelectedIndex() < 0) {
            cmbConvenio.setSelectedIndex(0);
        }
        cmbEstado.removeAllItems();
        cmbEstado.addItem(TODOS);
        for (EstadoTramite e : EstadoTramite.values()) {
            cmbEstado.addItem(e);
        }
        cmbEstado.setSelectedItem(estadoElegido == null ? TODOS : estadoElegido);
        actualizandoFiltros = false;
    }

    private void filtrosCambiaron() {
        if (!actualizandoFiltros) {
            actualizar();
        }
    }

    private Tramite tramiteSeleccionado() {
        String id = claveSeleccionada(tabla);
        if (id == null) {
            return null;
        }
        try {
            return getControl().buscarTramite(id);
        } catch (TramiteNoEncontradoException ex) {
            return null;
        }
    }

    private void mostrarDetalle() {
        modeloDocs.setRowCount(0);
        Tramite t = tramiteSeleccionado();
        if (t == null) {
            lblDetalle.setText("Ningún trámite seleccionado.");
            lblObservacion.setText(" ");
            return;
        }
        lblDetalle.setText(t.getIdTramite() + "  ·  " + t.getEstudiante().getNombre() + "  ·  "
                + t.getConvenio().getUniversidadSocia() + " (" + t.getConvenio().getPais() + ")  ·  "
                + t.getEstado().getEtiqueta());
        lblObservacion.setText(t.getObservacion().isEmpty() ? " " : "Observación: " + t.getObservacion());
        for (TipoDocumento req : t.getConvenio().getRequisitos()) {
            DocumentoSubido d = t.getDocumentos().get(req);
            modeloDocs.addRow(new Object[]{req.getEtiqueta(), d == null ? "Pendiente" : "Subido",
                d == null ? "" : d.getNombreArchivo(), d == null ? "" : d.getFechaSubida()});
        }
    }

    // ------------------------------------------------------------ acciones

    /** Crea un trámite eligiendo convenio y estudiante. */
    public void crear() {
        List<Convenio> convenios = getControl().listarConvenios();
        List<Estudiante> estudiantes = getControl().listarEstudiantes();
        if (convenios.isEmpty() || estudiantes.isEmpty()) {
            mostrarAviso("Debe existir al menos un convenio y un estudiante para crear un trámite.");
            return;
        }
        JComboBox<Convenio> cmbC = new JComboBox<>(convenios.toArray(new Convenio[0]));
        JComboBox<Estudiante> cmbE = new JComboBox<>(estudiantes.toArray(new Estudiante[0]));
        Object filtro = cmbConvenio.getSelectedItem();
        if (filtro instanceof Convenio) {
            cmbC.setSelectedItem(filtro);
        }
        JPanel formulario = new JPanel(new GridBagLayout());
        DialogoEstudiante.agregarFila(formulario, 0, "Convenio:", cmbC);
        DialogoEstudiante.agregarFila(formulario, 1, "Estudiante:", cmbE);
        if (JOptionPane.showConfirmDialog(this, formulario, "Nuevo trámite", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Convenio c = (Convenio) cmbC.getSelectedItem();
            Estudiante e = (Estudiante) cmbE.getSelectedItem();
            Tramite t = getControl().crearTramite(c.getIdConvenio(), e.getRut());
            txtBuscar.setText("");
            notificarCambio();
            seleccionarClave(tabla, t.getIdTramite());
            mostrarInfo("Trámite " + t.getIdTramite() + " creado.\nDocumentos exigidos: " + c.getRequisitos());
        } catch (ConvenioNoEncontradoException | EstudianteNoEncontradoException | RegistroDuplicadoException ex) {
            mostrarError(ex);
        }
    }

    /** Cambia el estado (aprobar, rechazar, reabrir) y la observación del trámite seleccionado. */
    public void editarSeleccionado() {
        Tramite t = tramiteSeleccionado();
        if (t == null) {
            mostrarAviso("Seleccione un trámite en la tabla.");
            return;
        }
        JComboBox<EstadoTramite> cmb = new JComboBox<>(EstadoTramite.values());
        cmb.setSelectedItem(t.getEstado());
        JTextArea obs = new JTextArea(t.getObservacion(), 3, 26);
        obs.setLineWrap(true);
        obs.setWrapStyleWord(true);
        JPanel formulario = new JPanel(new GridBagLayout());
        DialogoEstudiante.agregarFila(formulario, 0, "Nuevo estado:", cmb);
        DialogoEstudiante.agregarFila(formulario, 1, "Observación:", new JScrollPane(obs));
        JLabel ayuda = new JLabel("<html><small>Aprobar exige documentación completa. En proceso / Completo "
                + "reabren el trámite<br>y deben coincidir con los documentos subidos.</small></html>");
        DialogoEstudiante.agregarFila(formulario, 2, "", ayuda);
        if (JOptionPane.showConfirmDialog(this, formulario, "Cambiar estado de " + t.getIdTramite(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            getControl().editarTramite(t.getIdTramite(), (EstadoTramite) cmb.getSelectedItem(), obs.getText());
            notificarCambio();
        } catch (TramiteNoEncontradoException | DatoInvalidoException ex) {
            mostrarError(ex);
        }
    }

    /** Elimina el trámite seleccionado previa confirmación. */
    public void eliminarSeleccionado() {
        Tramite t = tramiteSeleccionado();
        if (t == null) {
            mostrarAviso("Seleccione un trámite en la tabla.");
            return;
        }
        if (!confirmar("¿Eliminar el trámite " + t + "?")) {
            return;
        }
        try {
            getControl().eliminarTramite(t.getIdTramite());
            notificarCambio();
        } catch (TramiteNoEncontradoException ex) {
            mostrarError(ex);
        }
    }

    /** Sube un documento al trámite seleccionado; si ya existe, pregunta si se reemplaza. */
    public void subirDocumento() {
        Tramite t = tramiteSeleccionado();
        if (t == null) {
            mostrarAviso("Seleccione un trámite en la tabla.");
            return;
        }
        JComboBox<TipoDocumento> cmbTipo = new JComboBox<>(t.getConvenio().getRequisitos().toArray(new TipoDocumento[0]));
        if (!t.getDocumentosFaltantes().isEmpty()) {
            cmbTipo.setSelectedItem(t.getDocumentosFaltantes().iterator().next());
        }
        JTextField txtArchivo = new JTextField(22);
        JButton btnExaminar = new JButton("Examinar...");
        btnExaminar.addActionListener(e -> {
            JFileChooser selector = new JFileChooser();
            if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivo = selector.getSelectedFile();
                txtArchivo.setText(archivo.getName());
            }
        });
        JPanel filaArchivo = new JPanel(new BorderLayout(4, 0));
        filaArchivo.add(txtArchivo, BorderLayout.CENTER);
        filaArchivo.add(btnExaminar, BorderLayout.EAST);
        JPanel formulario = new JPanel(new GridBagLayout());
        DialogoEstudiante.agregarFila(formulario, 0, "Documento:", cmbTipo);
        DialogoEstudiante.agregarFila(formulario, 1, "Archivo:", filaArchivo);
        if (JOptionPane.showConfirmDialog(this, formulario, "Subir documento a " + t.getIdTramite(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        TipoDocumento tipo = (TipoDocumento) cmbTipo.getSelectedItem();
        try {
            try {
                getControl().subirDocumento(t.getIdTramite(), tipo, txtArchivo.getText(), false);
            } catch (DocumentoDuplicadoException dup) {
                if (!confirmar(dup.getMessage() + "\n¿Desea reemplazarlo?")) {
                    return;
                }
                getControl().subirDocumento(t.getIdTramite(), tipo, txtArchivo.getText(), true);
            }
            notificarCambio();
        } catch (TramiteNoEncontradoException | DocumentoDuplicadoException | DatoInvalidoException ex) {
            mostrarError(ex);
        }
    }

    /** Quita un documento del trámite seleccionado (el elegido en la tabla de documentos, o se pregunta). */
    public void quitarDocumento() {
        Tramite t = tramiteSeleccionado();
        if (t == null) {
            mostrarAviso("Seleccione un trámite en la tabla.");
            return;
        }
        if (t.getDocumentos().isEmpty()) {
            mostrarAviso("El trámite " + t.getIdTramite() + " no tiene documentos subidos.");
            return;
        }
        TipoDocumento[] subidos = t.getDocumentos().keySet().toArray(new TipoDocumento[0]);
        TipoDocumento tipo = (TipoDocumento) JOptionPane.showInputDialog(this, "Documento a quitar:",
                "Quitar documento de " + t.getIdTramite(), JOptionPane.QUESTION_MESSAGE, null, subidos, subidos[0]);
        if (tipo == null) {
            return;
        }
        try {
            getControl().quitarDocumento(t.getIdTramite(), tipo);
            notificarCambio();
        } catch (TramiteNoEncontradoException | DatoInvalidoException ex) {
            mostrarError(ex);
        }
    }

    /** Lleva el cursor al campo de búsqueda. */
    public void enfocarBusqueda() {
        txtBuscar.requestFocusInWindow();
        txtBuscar.selectAll();
    }

    /** Colorea las celdas de estado para leer la tabla de un vistazo. */
    private static class RenderEstado extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            if (!seleccionada) {
                c.setForeground(colorPara(String.valueOf(valor)));
            }
            return c;
        }

        private static Color colorPara(String texto) {
            if (texto.equals(EstadoTramite.APROBADO.getEtiqueta()) || texto.equals("Subido")) {
                return new Color(0, 128, 60);
            }
            if (texto.equals(EstadoTramite.RECHAZADO.getEtiqueta()) || texto.equals("Pendiente")) {
                return new Color(190, 30, 30);
            }
            if (texto.equals(EstadoTramite.COMPLETO.getEtiqueta())) {
                return new Color(20, 90, 170);
            }
            return new Color(170, 110, 0);
        }
    }
}
