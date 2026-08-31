package com.mycompany.mavenproject1;

import java.io.*;
import java.util.*;

public class ConsoleApp {

    private static Control control = new Control();
    private static DataStore dataStore = new DataStore(control);
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  SISTEMA DE INTERCAMBIO ESTUDIANTIL - CONSOLA COMPLETA");
        System.out.println("============================================================");
        cargarDatos();

        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            int opcion = leerInt("Opcion");
            System.out.println();
            switch (opcion) {
                case 1: registrarEstudiante(); break;
                case 2: listarEstudiantes(); break;
                case 3: editarEstudiante(); break;
                case 4: eliminarEstudiante(); break;
                case 5: registrarConvenio(); break;
                case 6: listarConvenios(); break;
                case 7: editarConvenio(); break;
                case 8: eliminarConvenio(); break;
                case 9: gestionarRequisitosConvenio(); break;
                case 10: crearTramite(); break;
                case 11: listarTramites(); break;
                case 12: editarTramite(); break;
                case 13: eliminarTramite(); break;
                case 14: gestionarDocumentos(); break;
                case 15: mostrarDetallesTramite(); break;
                case 16: buscarPorNiveles(); break;
                case 17: buscarGlobal(); break;
                case 18: exportarDatos(); break;
                case 0: salir = confirmarSalida(); break;
                default: System.out.println("Opcion invalida");
            }
        }
        scanner.close();
    }

    private static void cargarDatos() {
        try {
            dataStore.load();
            System.out.println("Datos cargados: " + control.getEstudiantes().size() + " estudiantes, " + control.getConvenios().size() + " convenios.");
        } catch (Exception e) {
            System.out.println("No se encontraron datos, creando iniciales.");
            control.datos();
            guardar();
        }
    }

    private static void guardar() {
        try {
            dataStore.save();
            System.out.println(">> Datos guardados.");
        } catch (Exception e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n========== MENU PRINCIPAL ==========");
        System.out.println("=== ESTUDIANTES ===");
        System.out.println("  1. Registrar estudiante");
        System.out.println("  2. Listar estudiantes");
        System.out.println("  3. Editar estudiante");
        System.out.println("  4. Eliminar estudiante");
        System.out.println("=== CONVENIOS ===");
        System.out.println("  5. Registrar convenio");
        System.out.println("  6. Listar convenios");
        System.out.println("  7. Editar convenio");
        System.out.println("  8. Eliminar convenio");
        System.out.println("  9. Gestionar requisitos de convenio");
        System.out.println("=== TRAMITES ===");
        System.out.println(" 10. Crear tramite");
        System.out.println(" 11. Listar tramites");
        System.out.println(" 12. Editar tramite (estado o estudiante)");
        System.out.println(" 13. Eliminar tramite");
        System.out.println(" 14. Subir/Eliminar documento");
        System.out.println(" 15. Ver detalles de un tramite");
        System.out.println("=== BUSQUEDA ===");
        System.out.println(" 16. Busqueda por niveles (1,2,3)");
        System.out.println(" 17. Busqueda global");
        System.out.println("=== OTRAS ===");
        System.out.println(" 18. Exportar todo a TXT");
        System.out.println("  0. Salir");
    }

    // Lectura robusta
    private static int leerInt(String mensaje) {
        while (true) {
            System.out.println(mensaje);
            System.out.print("> ");
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero entero.");
            }
        }
    }

    private static String leerLinea(String mensaje) {
        System.out.println(mensaje);
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    private static boolean leerSiNo(String mensaje) {
        String r = leerLinea(mensaje + " (s/n)").toLowerCase();
        return r.equals("s") || r.equals("si");
    }

    // ==================== ESTUDIANTES ====================
    private static void registrarEstudiante() {
        System.out.println("\n--- REGISTRAR ESTUDIANTE ---");
        String rut = leerLinea("RUT (ej: 12.345.678-9)");
        if (!validarRUT(rut)) {
            System.out.println("RUT invalido.");
            return;
        }
        if (control.buscarEstudiante(rut) != null) {
            System.out.println("Ya existe un estudiante con ese RUT.");
            return;
        }
        String nombre = leerLinea("Nombre completo");
        String carrera = leerLinea("Carrera");
        int anio = Integer.parseInt(leerLinea("Año de ingreso"));
        control.registrarEstudiante(rut, nombre, carrera, anio);
        System.out.println("Estudiante registrado.");
        guardar();
    }

    private static void listarEstudiantes() {
        System.out.println("\n--- LISTA DE ESTUDIANTES ---");
        var estudiantes = control.getEstudiantes();
        if (estudiantes.isEmpty()) {
            System.out.println("No hay estudiantes.");
            return;
        }
        System.out.printf("%-15s | %-25s | %-20s | %-4s | %-12s%n",
                "RUT", "NOMBRE", "CARRERA", "ANO", "ESTADO");
        System.out.println("-".repeat(85));
        for (Estudiante e : estudiantes) {
            System.out.printf("%-15s | %-25s | %-20s | %-4d | %-12s%n",
                    e.getRut(), truncar(e.getNombre(), 25), truncar(e.getCarrera(), 20),
                    e.getAnioIngreso(), e.getEstadoProceso() == null ? "-" : e.getEstadoProceso());
        }
    }

    private static void editarEstudiante() {
        System.out.println("\n--- EDITAR ESTUDIANTE ---");
        String rut = leerLinea("RUT del estudiante a editar");
        Estudiante e = control.buscarEstudiante(rut);
        if (e == null) {
            System.out.println("Estudiante no existe.");
            return;
        }
        System.out.println("Deje en blanco para no modificar.");
        String nombre = leerLinea("Nuevo nombre (" + e.getNombre() + ")");
        String carrera = leerLinea("Nueva carrera (" + e.getCarrera() + ")");
        String anioStr = leerLinea("Nuevo año (" + e.getAnioIngreso() + ")");
        String estado = leerLinea("Nuevo estado (" + e.getEstadoProceso() + ")");
        Integer anio = anioStr.isBlank() ? null : Integer.parseInt(anioStr);
        boolean ok = control.editarEstudiante(rut,
                nombre.isBlank() ? null : nombre,
                carrera.isBlank() ? null : carrera,
                anio,
                estado.isBlank() ? null : estado);
        if (ok) {
            System.out.println("Estudiante actualizado.");
            guardar();
        } else {
            System.out.println("Error al actualizar.");
        }
    }

    private static void eliminarEstudiante() {
        System.out.println("\n--- ELIMINAR ESTUDIANTE ---");
        String rut = leerLinea("RUT del estudiante");
        Estudiante e = control.buscarEstudiante(rut);
        if (e == null) {
            System.out.println("No existe.");
            return;
        }
        if (leerSiNo("Eliminar estudiante y sus tramites asociados")) {
            boolean ok = control.eliminarEstudiante(rut);
            if (ok) {
                System.out.println("Estudiante eliminado.");
                guardar();
            } else {
                System.out.println("Error al eliminar.");
            }
        }
    }

    // ==================== CONVENIOS ====================
    private static void registrarConvenio() {
        System.out.println("\n--- REGISTRAR CONVENIO ---");
        String id = leerLinea("ID (ej: X-2026)");
        if (control.buscarConvenio(id) != null) {
            System.out.println("Ya existe un convenio con ese ID.");
            return;
        }
        String nombre = leerLinea("Nombre");
        String uni = leerLinea("Universidad socia");
        String pais = leerLinea("Pais");
        String duracion = leerLinea("Duracion (ej: '6 meses')");
        String carrera = leerLinea("Carrera asociada");
        Set<TipoDocumento> req = new HashSet<>();
        System.out.println("Requisitos (documentos):");
        for (TipoDocumento td : TipoDocumento.values()) {
            if (leerSiNo("  Incluir " + td))
                req.add(td);
        }
        Convenio c = new Convenio(id, nombre, uni, pais, req, duracion, carrera);
        control.agregarConvenio(c);
        System.out.println("Convenio registrado.");
        guardar();
    }

    private static void listarConvenios() {
        System.out.println("\n--- LISTA DE CONVENIOS ---");
        var convenios = control.getConvenios();
        if (convenios.isEmpty()) {
            System.out.println("No hay convenios.");
            return;
        }
        for (Convenio c : convenios) {
            System.out.println("ID: " + c.getIdConvenio());
            System.out.println("  Nombre: " + c.getNombre());
            System.out.println("  Universidad: " + c.getUniversidadSocia());
            System.out.println("  Pais: " + c.getPais());
            System.out.println("  Duracion: " + c.getDuracion());
            System.out.println("  Carrera: " + c.getCarreraAsociada());
            System.out.println("  Requisitos: " + c.getRequisitos());
            System.out.println("  Tramites: " + c.getTramites().size());
            System.out.println();
        }
    }

    private static void editarConvenio() {
        System.out.println("\n--- EDITAR CONVENIO ---");
        String id = leerLinea("ID del convenio a editar");
        Convenio c = control.buscarConvenio(id);
        if (c == null) {
            System.out.println("No existe.");
            return;
        }
        System.out.println("Deje en blanco para no modificar.");
        String nombre = leerLinea("Nuevo nombre (" + c.getNombre() + ")");
        String uni = leerLinea("Nueva universidad (" + c.getUniversidadSocia() + ")");
        String pais = leerLinea("Nuevo pais (" + c.getPais() + ")");
        String duracion = leerLinea("Nueva duracion (" + c.getDuracion() + ")");
        String carrera = leerLinea("Nueva carrera (" + c.getCarreraAsociada() + ")");
        boolean ok = control.editarConvenio(id,
                nombre.isBlank() ? null : nombre,
                uni.isBlank() ? null : uni,
                pais.isBlank() ? null : pais,
                duracion.isBlank() ? null : duracion,
                carrera.isBlank() ? null : carrera);
        if (ok) {
            System.out.println("Convenio actualizado.");
            guardar();
        } else {
            System.out.println("Error al actualizar.");
        }
    }

    private static void eliminarConvenio() {
        System.out.println("\n--- ELIMINAR CONVENIO ---");
        String id = leerLinea("ID del convenio");
        Convenio c = control.buscarConvenio(id);
        if (c == null) {
            System.out.println("No existe.");
            return;
        }
        if (leerSiNo("Eliminar convenio? Se eliminaran tambien sus tramites")) {
            boolean ok = control.eliminarConvenio(id);
            if (ok) {
                System.out.println("Convenio eliminado.");
                guardar();
            } else {
                System.out.println("Error al eliminar.");
            }
        }
    }

    private static void gestionarRequisitosConvenio() {
        System.out.println("\n--- GESTION DE REQUISITOS DE CONVENIO ---");
        String id = leerLinea("ID del convenio");
        Convenio c = control.buscarConvenio(id);
        if (c == null) {
            System.out.println("No existe.");
            return;
        }
        System.out.println("Requisitos actuales: " + c.getRequisitos());
        System.out.println("1. Agregar requisito");
        System.out.println("2. Quitar requisito");
        int op = leerInt("Opcion");
        TipoDocumento[] valores = TipoDocumento.values();
        if (op == 1) {
            System.out.println("Tipos disponibles:");
            for (int i = 0; i < valores.length; i++)
                System.out.println("  " + (i+1) + ". " + valores[i]);
            int idx = Integer.parseInt(leerLinea("Numero")) - 1;
            if (idx >= 0 && idx < valores.length) {
                c.agregarRequisito(valores[idx]);
                System.out.println("Requisito agregado.");
                guardar();
            } else {
                System.out.println("Numero invalido.");
            }
        } else if (op == 2) {
            List<TipoDocumento> lista = new ArrayList<>(c.getRequisitos());
            if (lista.isEmpty()) {
                System.out.println("No hay requisitos para quitar.");
                return;
            }
            System.out.println("Requisitos actuales:");
            for (int i = 0; i < lista.size(); i++)
                System.out.println("  " + (i+1) + ". " + lista.get(i));
            int idx = Integer.parseInt(leerLinea("Numero a quitar")) - 1;
            if (idx >= 0 && idx < lista.size()) {
                c.quitarRequisito(lista.get(idx));
                System.out.println("Requisito eliminado.");
                guardar();
            } else {
                System.out.println("Numero invalido.");
            }
        } else {
            System.out.println("Opcion invalida.");
        }
    }

    // ==================== TRAMITES ====================
    private static void crearTramite() {
        System.out.println("\n--- CREAR TRAMITE ---");
        String idConv = leerLinea("ID del convenio");
        Convenio conv = control.buscarConvenio(idConv);
        if (conv == null) {
            System.out.println("Convenio no existe.");
            return;
        }
        String rut = leerLinea("RUT del estudiante");
        Estudiante est = control.buscarEstudiante(rut);
        if (est == null) {
            System.out.println("Estudiante no existe.");
            return;
        }
        Tramite t = conv.crearTramite(est);
        System.out.println("Tramite creado con ID: " + t.getIdTramite());
        guardar();
    }

    private static void listarTramites() {
        System.out.println("\n--- LISTA DE TRAMITES ---");
        boolean hay = false;
        for (Convenio c : control.getConvenios()) {
            if (!c.getTramites().isEmpty()) {
                hay = true;
                System.out.println("\nConvenio: " + c.getIdConvenio() + " - " + c.getNombre());
                for (Tramite t : c.getTramites()) {
                    String estudiante = t.getEstudiante() == null ? "Sin asignar" : t.getEstudiante().getRut();
                    System.out.printf("  %s | Est: %s | Estado: %s | Docs: %d/%d%n",
                            t.getIdTramite(), estudiante, t.getEstado(),
                            t.getDocumentos().size(), c.getRequisitos().size());
                }
            }
        }
        if (!hay) System.out.println("No hay tramites.");
    }

    private static void editarTramite() {
        System.out.println("\n--- EDITAR TRAMITE ---");
        String idConv = leerLinea("ID del convenio");
        Convenio conv = control.buscarConvenio(idConv);
        if (conv == null) {
            System.out.println("Convenio no existe.");
            return;
        }
        String idTram = leerLinea("ID del tramite");
        Tramite t = conv.getTramites().stream().filter(tr -> tr.getIdTramite().equals(idTram)).findFirst().orElse(null);
        if (t == null) {
            System.out.println("Tramite no existe.");
            return;
        }
        System.out.println("Estado actual: " + t.getEstado());
        String nuevoEstadoStr = leerLinea("Nuevo estado (EN_PROCESO/COMPLETO) [ENTER para mantener]");
        Tramite.Estado nuevoEstado = null;
        if (!nuevoEstadoStr.isBlank()) {
            try {
                nuevoEstado = Tramite.Estado.valueOf(nuevoEstadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Estado invalido, se mantiene el actual.");
            }
        }
        String nuevoRut = leerLinea("Nuevo RUT de estudiante [ENTER para mantener]");
        if (nuevoRut.isBlank()) nuevoRut = null;
        boolean ok = control.editarTramite(idConv, idTram, nuevoEstado, nuevoRut);
        if (ok) {
            System.out.println("Tramite actualizado.");
            guardar();
        } else {
            System.out.println("Error al actualizar (¿el nuevo estudiante existe?).");
        }
    }

    private static void eliminarTramite() {
        System.out.println("\n--- ELIMINAR TRAMITE ---");
        String idConv = leerLinea("ID del convenio");
        String idTram = leerLinea("ID del tramite");
        if (leerSiNo("Eliminar tramite")) {
            boolean ok = control.eliminarTramite(idConv, idTram);
            if (ok) {
                System.out.println("Tramite eliminado.");
                guardar();
            } else {
                System.out.println("Error al eliminar.");
            }
        }
    }

    private static void gestionarDocumentos() {
        System.out.println("\n--- SUBIR/ELIMINAR DOCUMENTO ---");
        String idConv = leerLinea("ID del convenio");
        Convenio conv = control.buscarConvenio(idConv);
        if (conv == null) {
            System.out.println("Convenio no existe.");
            return;
        }
        String idTram = leerLinea("ID del tramite");
        Tramite t = conv.getTramites().stream().filter(tr -> tr.getIdTramite().equals(idTram)).findFirst().orElse(null);
        if (t == null) {
            System.out.println("Tramite no existe.");
            return;
        }
        System.out.println("1. Subir documento");
        System.out.println("2. Eliminar documento");
        int op = leerInt("Opcion");
        if (op == 1) {
            System.out.println("Tipos disponibles: " + Arrays.toString(TipoDocumento.values()));
            String tipoStr = leerLinea("Tipo").toUpperCase();
            TipoDocumento tipo;
            try {
                tipo = TipoDocumento.valueOf(tipoStr);
            } catch (Exception e) {
                System.out.println("Tipo invalido.");
                return;
            }
            String archivo = leerLinea("Nombre del archivo");
            t.subirDocumento(tipo, archivo);
            conv.validarYActualizarEstado(t);
            System.out.println("Documento subido.");
            guardar();
        } else if (op == 2) {
            if (t.getDocumentos().isEmpty()) {
                System.out.println("No hay documentos.");
                return;
            }
            System.out.println("Documentos actuales: " + t.getDocumentos().keySet());
            String tipoStr = leerLinea("Tipo a eliminar").toUpperCase();
            TipoDocumento tipo;
            try {
                tipo = TipoDocumento.valueOf(tipoStr);
            } catch (Exception e) {
                System.out.println("Tipo invalido.");
                return;
            }
            if (t.eliminarDocumento(tipo)) {
                conv.validarYActualizarEstado(t);
                System.out.println("Documento eliminado.");
                guardar();
            } else {
                System.out.println("No existe ese documento.");
            }
        } else {
            System.out.println("Opcion invalida.");
        }
    }

    private static void mostrarDetallesTramite() {
        System.out.println("\n--- DETALLES DE TRAMITE ---");
        String idConv = leerLinea("ID del convenio");
        Convenio conv = control.buscarConvenio(idConv);
        if (conv == null) {
            System.out.println("Convenio no existe.");
            return;
        }
        String idTram = leerLinea("ID del tramite");
        Tramite t = conv.getTramites().stream().filter(tr -> tr.getIdTramite().equals(idTram)).findFirst().orElse(null);
        if (t == null) {
            System.out.println("Tramite no existe.");
            return;
        }
        System.out.println("\n=== DETALLE DEL TRAMITE ===");
        System.out.println("ID: " + t.getIdTramite());
        System.out.println("Estudiante: " + (t.getEstudiante() == null ? "N/A" : t.getEstudiante().getNombre() + " (" + t.getEstudiante().getRut() + ")"));
        System.out.println("Estado: " + t.getEstado());
        System.out.println("Documentos subidos: " + t.getDocumentos().size());
        System.out.println("Requisitos del convenio:");
        for (TipoDocumento req : conv.getRequisitos()) {
            System.out.print("  - " + req);
            if (t.getDocumentos().containsKey(req))
                System.out.println(" ✓ (Archivo: " + t.getDocumentos().get(req).getNombreArchivo() + ")");
            else
                System.out.println(" ✗ (Pendiente)");
        }
    }

    // ==================== BUSQUEDAS ====================
    private static void buscarPorNiveles() {
        System.out.println("\n--- BUSQUEDA POR NIVELES ---");
        System.out.println("1. Estudiantes por nombre");
        System.out.println("2. Convenios por ID");
        System.out.println("3. Tramites por texto (ID, estudiante, estado)");
        int nivel = leerInt("Nivel");
        String texto = leerLinea("Texto a buscar");
        if (nivel == 1) {
            var resultados = control.buscarEstudiantesPorNombre(texto);
            if (resultados.isEmpty()) System.out.println("No se encontraron estudiantes.");
            else {
                System.out.println("Estudiantes encontrados:");
                for (Estudiante e : resultados)
                    System.out.println("  " + e.getRut() + " - " + e.getNombre() + " (" + e.getCarrera() + ")");
            }
        } else if (nivel == 2) {
            var resultados = control.buscarConveniosPorId(texto);
            if (resultados.isEmpty()) System.out.println("No se encontraron convenios.");
            else {
                System.out.println("Convenios encontrados:");
                for (Convenio c : resultados)
                    System.out.println("  " + c.getIdConvenio() + " - " + c.getNombre() + " (" + c.getPais() + ")");
            }
        } else if (nivel == 3) {
            var resultados = control.buscarTramitesPorTexto(texto);
            if (resultados.isEmpty()) System.out.println("No se encontraron tramites.");
            else {
                System.out.println("Tramites encontrados:");
                for (Tramite t : resultados) {
                    Convenio padre = null;
                    for (Convenio c : control.getConvenios())
                        if (c.getTramites().contains(t)) { padre = c; break; }
                    System.out.println("  " + t.getIdTramite() + " | Conv: " + (padre==null?"?":padre.getIdConvenio()) +
                            " | Est: " + (t.getEstudiante()==null?"-":t.getEstudiante().getRut()) +
                            " | Estado: " + t.getEstado());
                }
            }
        } else {
            System.out.println("Nivel invalido.");
        }
    }

    private static void buscarGlobal() {
        String texto = leerLinea("Texto a buscar (nombre, RUT, ID, etc.)");
        List<String> resultados = control.buscarGlobal(texto);
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron resultados.");
        } else {
            System.out.println("Resultados:");
            for (String r : resultados)
                System.out.println("  " + r);
        }
    }

    // ==================== EXPORTAR ====================
    private static void exportarDatos() {
        System.out.println("\n--- EXPORTAR DATOS A TXT ---");
        String nombreArchivo = leerLinea("Nombre del archivo (ej: export.txt)");
        if (nombreArchivo.isBlank()) nombreArchivo = "export.txt";
        File destino = new File(nombreArchivo);
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destino), "UTF-8"))) {
            w.write("=== CONVENIOS ===\n");
            w.write("ID;Nombre;Universidad;Pais;Duracion;Carrera;Requisitos;Tramites\n");
            for (Convenio c : control.getConvenios()) {
                w.write(String.format("%s;%s;%s;%s;%s;%s;%d;%d\n",
                        escape(c.getIdConvenio()), escape(c.getNombre()), escape(c.getUniversidadSocia()),
                        escape(c.getPais()), escape(c.getDuracion()), escape(c.getCarreraAsociada()),
                        c.getRequisitos().size(), c.getTramites().size()));
            }
            w.write("\n=== ESTUDIANTES ===\n");
            w.write("RUT;Nombre;Carrera;Anio;Estado;Convenio\n");
            for (Estudiante e : control.getEstudiantes()) {
                w.write(String.format("%s;%s;%s;%d;%s;%s\n",
                        escape(e.getRut()), escape(e.getNombre()), escape(e.getCarrera()),
                        e.getAnioIngreso(), escape(e.getEstadoProceso()),
                        e.getConvenio() == null ? "" : e.getConvenio().getIdConvenio()));
            }
            w.write("\n=== TRAMITES ===\n");
            w.write("ID;Convenio;EstudianteRUT;Estado;Documentos\n");
            for (Convenio c : control.getConvenios()) {
                for (Tramite t : c.getTramites()) {
                    w.write(String.format("%s;%s;%s;%s;%s\n",
                            escape(t.getIdTramite()), escape(c.getIdConvenio()),
                            t.getEstudiante() == null ? "" : t.getEstudiante().getRut(),
                            t.getEstado().name(), t.getDocumentos().size()));
                }
            }
            System.out.println("Datos exportados a " + destino.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error al exportar: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(";", ",").replace("\n", " ");
    }

    // ==================== UTILIDADES ====================
    private static boolean validarRUT(String rut) {
        if (rut == null || rut.isBlank()) return false;
        String patron = "^\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]$";
        if (!rut.matches(patron)) return false;
        String rutLimpio = rut.replaceAll("[^\\dkK]", "");
        if (rutLimpio.length() < 2) return false;
        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        char dv = rutLimpio.charAt(rutLimpio.length() - 1);
        try {
            int suma = 0, mult = 2;
            for (int i = cuerpo.length() - 1; i >= 0; i--) {
                suma += (cuerpo.charAt(i) - '0') * mult;
                mult = mult == 7 ? 2 : mult + 1;
            }
            int resto = 11 - (suma % 11);
            char dvCalc = resto == 11 ? '0' : resto == 10 ? 'k' : (char) ('0' + resto);
            return Character.toLowerCase(dv) == Character.toLowerCase(dvCalc);
        } catch (Exception e) {
            return false;
        }
    }

    private static String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static boolean confirmarSalida() {
        if (leerSiNo("Guardar cambios antes de salir")) guardar();
        System.out.println("Saliendo...");
        return true;
    }
}