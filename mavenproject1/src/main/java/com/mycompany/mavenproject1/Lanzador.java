package com.mycompany.mavenproject1;

import javax.swing.JOptionPane;

/**
 * Punto de entrada unico del sistema (requisito SIA-10).
 *
 * Al iniciar la aplicacion se solicita al usuario decidir si desea
 * trabajar con la interfaz de consola o con la interfaz de ventanas.
 * Ambas modalidades se construyen sobre la misma logica de negocio
 * (clase Control) y el mismo motor de persistencia (clase DataStore).
 */
public class Lanzador {

    public static void main(String[] args) {

        String[] opciones = {"Ventana (GUI)", "Consola"};

        int eleccion = JOptionPane.showOptionDialog(
                null,
                "Seleccione el modo de ejecucion del sistema:",
                "Sistema de Gestion de Intercambio Estudiantil",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (eleccion == 1) {
            ConsoleApp.main(args);
        } else {
            Main.main(args);
        }
    }
}
