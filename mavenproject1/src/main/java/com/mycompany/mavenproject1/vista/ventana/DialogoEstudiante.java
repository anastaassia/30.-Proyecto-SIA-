package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.control.Validador;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import com.mycompany.mavenproject1.excepciones.EstudianteNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import com.mycompany.mavenproject1.modelo.EstadoProceso;
import com.mycompany.mavenproject1.modelo.Estudiante;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.Year;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Formulario para agregar un estudiante nuevo o editar uno existente.
 */
public class DialogoEstudiante extends JDialog {

    private static final long serialVersionUID = 1L;

    private Control control;
    private Estudiante estudiante;
    private boolean guardado;

    private final JTextField txtRut = new JTextField(22);
    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtCarrera = new JTextField(22);
    private final JSpinner spnAnio;
    private final JComboBox<EstadoProceso> cmbEstado = new JComboBox<>(EstadoProceso.values());

    /**
     * @param duenio     ventana sobre la que se abre
     * @param control    controlador del sistema
     * @param estudiante estudiante a editar, o null para agregar uno nuevo
     */
    public DialogoEstudiante(Window duenio, Control control, Estudiante estudiante) {
        super(duenio, estudiante == null ? "Agregar estudiante" : "Editar estudiante", ModalityType.APPLICATION_MODAL);
        this.control = control;
        this.estudiante = estudiante;
        int anioActual = Year.now().getValue();
        spnAnio = new JSpinner(new SpinnerNumberModel(anioActual, Validador.ANIO_MINIMO, anioActual, 1));
        spnAnio.setEditor(new JSpinner.NumberEditor(spnAnio, "#"));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));
        int fila = 0;
        agregarFila(formulario, fila++, "RUT:", txtRut);
        agregarFila(formulario, fila++, "Nombre completo:", txtNombre);
        agregarFila(formulario, fila++, "Carrera:", txtCarrera);
        agregarFila(formulario, fila++, "Año de ingreso:", spnAnio);
        agregarFila(formulario, fila, "Estado del proceso:", cmbEstado);

        if (estudiante == null) {
            txtRut.setToolTipText("Con o sin puntos, ej. 12.345.678-5");
            cmbEstado.setSelectedItem(EstadoProceso.POSTULACION);
            cmbEstado.setEnabled(false);
        } else {
            txtRut.setText(estudiante.getRut());
            txtRut.setEnabled(false);
            txtNombre.setText(estudiante.getNombre());
            txtCarrera.setText(estudiante.getCarrera());
            spnAnio.setValue(estudiante.getAnioIngreso());
            cmbEstado.setSelectedItem(estudiante.getEstadoProceso());
        }

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        setLayout(new BorderLayout());
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);
        pack();
        setResizable(false);
        setLocationRelativeTo(duenio);
    }

    static void agregarFila(JPanel panel, int fila, String etiqueta, Component campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void guardar() {
        int anio = (Integer) spnAnio.getValue();
        try {
            if (estudiante == null) {
                Estudiante nuevo = new Estudiante(txtRut.getText(), txtNombre.getText(), txtCarrera.getText(), anio);
                control.registrarEstudiante(nuevo);
                estudiante = nuevo;
            } else {
                control.editarEstudiante(estudiante.getRut(), txtNombre.getText(), txtCarrera.getText(), anio,
                        (EstadoProceso) cmbEstado.getSelectedItem());
            }
            guardado = true;
            dispose();
        } catch (DatoInvalidoException | RegistroDuplicadoException | EstudianteNoEncontradoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos no válidos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** @return true si el usuario guardó los cambios */
    public boolean isGuardado() {
        return guardado;
    }

    /** @return el estudiante creado o editado (null si se canceló un alta) */
    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }
}
