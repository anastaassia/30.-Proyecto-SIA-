package com.mycompany.mavenproject1;

import com.mycompany.mavenproject1.control.Control;
import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import com.mycompany.mavenproject1.excepciones.RegistroDuplicadoException;
import com.mycompany.mavenproject1.persistencia.DataStore;
import com.mycompany.mavenproject1.vista.consola.ConsoleApp;
import com.mycompany.mavenproject1.vista.ventana.VentanaPrincipal;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Punto de entrada único del sistema (SIA-10).
 * <ol>
 *   <li>Carga los datos desde los CSV (persistencia batch, SIA-11). Si no existen,
 *       carga los datos iniciales de ejemplo.</li>
 *   <li>Pregunta si se usará la consola o las ventanas.</li>
 *   <li>Inicia la interfaz elegida. Ambas trabajan sobre el mismo {@link Control}.</li>
 * </ol>
 */
public final class Lanzador {

    private Lanzador() {
    }

    /**
     * @param args no se usan
     */
    public static void main(String[] args) {
        Control control = new Control();
        DataStore dataStore = new DataStore(control);
        cargarDatos(control, dataStore);

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("No hay entorno gráfico disponible: se inicia la consola.");
            new ConsoleApp(control, dataStore).iniciar();
            return;
        }
        aplicarAspecto();
        String[] opciones = {"Ventanas", "Consola"};
        int eleccion = JOptionPane.showOptionDialog(null,
                "¿Cómo desea usar el sistema?",
                "Sistema de Gestión de Intercambio Estudiantil",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (eleccion == 0) {
            SwingUtilities.invokeLater(() -> {
                VentanaPrincipal ventana = new VentanaPrincipal(control, dataStore);
                ventana.setVisible(true);
                ventana.mostrarAdvertenciasDeCarga();
            });
        } else if (eleccion == 1) {
            new ConsoleApp(control, dataStore).iniciar();
            System.exit(0);
        } else {
            System.exit(0); // cerró el diálogo sin elegir
        }
    }

    /**
     * Carga los CSV. Si no hay archivos (primera ejecución), usa los datos de ejemplo.
     * Si un archivo no se puede leer, NO se cargan datos de ejemplo encima, para no
     * sobrescribir los datos reales al salir.
     */
    private static void cargarDatos(Control control, DataStore dataStore) {
        try {
            if (dataStore.existenArchivos()) {
                dataStore.cargar();
            } else {
                System.out.println("No se encontraron archivos de datos: se cargan los datos de ejemplo.");
                control.cargarDatosIniciales();
            }
        } catch (IOException ex) {
            System.err.println("No se pudieron leer los archivos de datos: " + ex.getMessage());
            System.err.println("Revise la carpeta " + dataStore.getCarpeta().toAbsolutePath());
            System.exit(1);
        } catch (DatoInvalidoException | RegistroDuplicadoException ex) {
            System.err.println("Error en los datos de ejemplo: " + ex.getMessage());
        }
    }

    private static void aplicarAspecto() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                 | UnsupportedLookAndFeelException ex) {
            System.err.println("No se pudo aplicar el aspecto Nimbus; se usa el predeterminado.");
        }
    }
}
