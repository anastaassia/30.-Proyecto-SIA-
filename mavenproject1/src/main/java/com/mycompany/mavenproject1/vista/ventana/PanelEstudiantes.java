package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.excepciones.EstudianteNoEncontradoException;
import com.mycompany.mavenproject1.modelo.Estudiante;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Panel de estudiantes: la tabla es el listado; desde aquí se agrega, edita,
 * elimina y busca. Todo en un solo lugar.
 */
public class PanelEstudiantes extends PanelBase {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modelo = crearModelo("RUT", "Nombre", "Carrera", "Año ingreso", "Estado");
    private final JTable tabla = crearTabla(modelo);
    private final JTextField txtBuscar = crearBuscador(this::actualizar);
    private final JLabel lblTotal = new JLabel();

    /**
     * @param control controlador del sistema
     */
    public PanelEstudiantes(Control control) {
        super(control, "Estudiantes", "Estudiantes que pueden postular a convenios. Doble clic en una fila para editarla.");

        JPanel barra = crearBarra();
        barra.add(crearBoton("Agregar", e -> agregar()));
        barra.add(crearBoton("Editar", e -> editarSeleccionado()));
        barra.add(crearBoton("Eliminar", e -> eliminarSeleccionado()));
        barra.add(new JLabel("     Buscar:"));
        barra.add(txtBuscar);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(220);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarSeleccionado();
                }
            }
        });

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setOpaque(false);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        centro.add(lblTotal, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        String seleccion = claveSeleccionada(tabla);
        modelo.setRowCount(0);
        for (Estudiante e : getControl().buscarEstudiantes(txtBuscar.getText())) {
            modelo.addRow(new Object[]{e.getRut(), e.getNombre(), e.getCarrera(), e.getAnioIngreso(),
                e.getEstadoProceso()});
        }
        seleccionarClave(tabla, seleccion);
        lblTotal.setText(modelo.getRowCount() + " de " + getControl().getEstudiantes().size() + " estudiantes");
    }

    /** Abre el formulario para registrar un estudiante nuevo. */
    public void agregar() {
        DialogoEstudiante dialogo = new DialogoEstudiante(SwingUtilities.getWindowAncestor(this), getControl(), null);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            txtBuscar.setText("");
            notificarCambio();
            seleccionarClave(tabla, dialogo.getEstudiante().getRut());
        }
    }

    /** Abre el formulario de edición del estudiante seleccionado. */
    public void editarSeleccionado() {
        String rut = claveSeleccionada(tabla);
        if (rut == null) {
            mostrarAviso("Seleccione un estudiante en la tabla.");
            return;
        }
        try {
            Estudiante e = getControl().buscarEstudiante(rut);
            DialogoEstudiante dialogo = new DialogoEstudiante(SwingUtilities.getWindowAncestor(this), getControl(), e);
            dialogo.setVisible(true);
            if (dialogo.isGuardado()) {
                notificarCambio();
            }
        } catch (EstudianteNoEncontradoException ex) {
            mostrarError(ex);
        }
    }

    /** Elimina el estudiante seleccionado (y sus trámites) previa confirmación. */
    public void eliminarSeleccionado() {
        String rut = claveSeleccionada(tabla);
        if (rut == null) {
            mostrarAviso("Seleccione un estudiante en la tabla.");
            return;
        }
        try {
            Estudiante e = getControl().buscarEstudiante(rut);
            if (confirmar("¿Eliminar a " + e + "?\nTambién se eliminarán sus trámites.")) {
                int tramites = getControl().eliminarEstudiante(rut);
                notificarCambio();
                mostrarInfo("Estudiante eliminado junto con " + tramites + " trámite(s).");
            }
        } catch (EstudianteNoEncontradoException ex) {
            mostrarError(ex);
        }
    }

    /** Lleva el cursor al campo de búsqueda. */
    public void enfocarBusqueda() {
        txtBuscar.requestFocusInWindow();
        txtBuscar.selectAll();
    }
}
