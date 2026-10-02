package com.mycompany.mavenproject1.vista.ventana;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.excepciones.ConvenioNoEncontradoException;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import com.mycompany.mavenproject1.modelo.Convenio;
import com.mycompany.mavenproject1.modelo.TipoDocumento;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Formulario para agregar un convenio nuevo o editar uno existente, incluidos
 * los documentos que exige (requisitos).
 */
public class DialogoConvenio extends JDialog {

    private static final long serialVersionUID = 1L;

    private Control control;
    private Convenio convenio;
    private boolean guardado;

    private final JTextField txtId = new JTextField(24);
    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtUniversidad = new JTextField(24);
    private final JTextField txtPais = new JTextField(24);
    private final JSpinner spnMeses = new JSpinner(new SpinnerNumberModel(6, 1, 24, 1));
    private final JTextField txtCarrera = new JTextField(24);
    private final Map<TipoDocumento, JCheckBox> checks = new EnumMap<>(TipoDocumento.class);

    /**
     * @param duenio   ventana sobre la que se abre
     * @param control  controlador del sistema
     * @param convenio convenio a editar, o null para agregar uno nuevo
     */
    public DialogoConvenio(Window duenio, Control control, Convenio convenio) {
        super(duenio, convenio == null ? "Agregar convenio" : "Editar convenio", ModalityType.APPLICATION_MODAL);
        this.control = control;
        this.convenio = convenio;

        JPanel formulario = new JPanel(new java.awt.GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));
        int fila = 0;
        DialogoEstudiante.agregarFila(formulario, fila++, "ID del convenio:", txtId);
        DialogoEstudiante.agregarFila(formulario, fila++, "Nombre:", txtNombre);
        DialogoEstudiante.agregarFila(formulario, fila++, "Universidad socia:", txtUniversidad);
        DialogoEstudiante.agregarFila(formulario, fila++, "País:", txtPais);
        DialogoEstudiante.agregarFila(formulario, fila++, "Duración (meses):", spnMeses);
        DialogoEstudiante.agregarFila(formulario, fila++, "Carrera asociada:", txtCarrera);

        JPanel panelRequisitos = new JPanel(new GridLayout(0, 2, 6, 2));
        panelRequisitos.setBorder(BorderFactory.createTitledBorder("Documentos exigidos para postular"));
        for (TipoDocumento td : TipoDocumento.values()) {
            JCheckBox check = new JCheckBox(td.getEtiqueta());
            checks.put(td, check);
            panelRequisitos.add(check);
        }

        if (convenio == null) {
            txtId.setToolTipText("Sin espacios, ej. IT-POLIMI-2026");
        } else {
            txtId.setText(convenio.getIdConvenio());
            txtId.setEnabled(false);
            txtNombre.setText(convenio.getNombre());
            txtUniversidad.setText(convenio.getUniversidadSocia());
            txtPais.setText(convenio.getPais());
            spnMeses.setValue(convenio.getDuracionMeses());
            txtCarrera.setText(convenio.getCarreraAsociada());
            for (TipoDocumento td : convenio.getRequisitos()) {
                checks.get(td).setSelected(true);
            }
        }

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(formulario, BorderLayout.CENTER);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        envoltorio.add(panelRequisitos);
        centro.add(envoltorio, BorderLayout.SOUTH);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        setLayout(new BorderLayout());
        add(centro, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);
        pack();
        setResizable(false);
        setLocationRelativeTo(duenio);
    }

    private Set<TipoDocumento> requisitosMarcados() {
        Set<TipoDocumento> marcados = EnumSet.noneOf(TipoDocumento.class);
        for (Map.Entry<TipoDocumento, JCheckBox> e : checks.entrySet()) {
            if (e.getValue().isSelected()) {
                marcados.add(e.getKey());
            }
        }
        return marcados;
    }

    private void guardar() {
        int meses = (Integer) spnMeses.getValue();
        try {
            if (convenio == null) {
                Convenio nuevo = new Convenio(txtId.getText(), txtNombre.getText(), txtUniversidad.getText(),
                        txtPais.getText(), meses, txtCarrera.getText(), requisitosMarcados());
                control.agregarConvenio(nuevo);
                convenio = nuevo;
            } else {
                control.editarConvenio(convenio.getIdConvenio(), txtNombre.getText(), txtUniversidad.getText(),
                        txtPais.getText(), meses, txtCarrera.getText(), requisitosMarcados());
            }
            guardado = true;
            dispose();
        } catch (DatoInvalidoException | RegistroDuplicadoException | ConvenioNoEncontradoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos no válidos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** @return true si el usuario guardó los cambios */
    public boolean isGuardado() {
        return guardado;
    }

    /** @return el convenio creado o editado (null si se canceló un alta) */
    public Convenio getConvenio() {
        return convenio;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }
}
