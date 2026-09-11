# Cambios aplicados

Verificado: compila con `javac --release 8` y `--release 11`, cero errores.
Probado en ejecucion: guardar -> recargar -> eliminar tramite -> eliminar estudiante.

## 1. Compatibilidad Java 8 (para que corra en JDK 8 y JDK 11)

- `pom.xml`: se quito `maven.compiler.release` (no existe en JDK 8) y se puso
  `maven.compiler.source/target = 1.8`.
- `String.isBlank()` -> `.trim().isEmpty()` (33 reemplazos, 7 archivos)
- `var` -> tipo explicito (ConsoleApp: 5 casos)
- `List.copyOf(...)` -> `Collections.unmodifiableList(new ArrayList<>(...))` (Control:15)
- `"-".repeat(85)` -> metodo `repetir(String,int)` nuevo en ConsoleApp

## 2. UnsupportedOperationException al eliminar y crear tramites

`Convenio.getTramites()` devuelve una lista inmutable, pero 4 lugares la modificaban.

Metodos nuevos en `Convenio`: `agregarTramite`, `eliminarTramite`,
`eliminarTramitesDeEstudiante`.

Llamadas corregidas:
- Control.java  (eliminarTramite)
- Control.java  (eliminarEstudiante)
- DataStore.java (carga de tramites)
- PanelTramites.java (crear tramite con ID manual)

## 3. Persistencia (SIA-11)

`split(";")` descarta los campos vacios del final, por lo que las lineas de
estudiante terminadas en `;` quedaban con 5 campos y se descartaban en silencio.

`DataStore`: `.split(";")` -> `.split(";", -1)` (4 reemplazos)

## PENDIENTE (ver plan-mejoras-sia.md)

- SIA-10: falta la clase Lanzador (selector consola/ventana)
- SIA-4: Convenio.tramites deberia ser un Map
- Corregir las 6 afirmaciones falsas del informe
- Seccion SIA-13 y README de instalacion
