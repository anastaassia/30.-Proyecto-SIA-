# Sistema de Gestión de Programas de Intercambio Estudiantil

Proyecto SIA — INF2236 Programación Avanzada — 2026-1 
Grupo 30: Anastasia Acuña · Alejandro Lanas · Nicolás Echeverría 
Sistema que gestiona las postulaciones de estudiantes a convenios de intercambio: 
estudiantes, convenios (con los documentos que exige cada uno), trámites de postulación, 
documentos subidos y reportes de seguimiento.
---

## Requisitos

- **Oracle JDK 11 u Oracle JDK 8** (el código es compatible con Java 8).
- **NetBeans 21 o inferior** (proyecto Maven) o Eclipse.
- No usa librerías externas: solo la biblioteca estándar de Java.

## Instalación y ejecución en NetBeans

1. Descomprimir el proyecto (o clonar el repositorio).
2. NetBeans → **File → Open Project** → seleccionar la carpeta **`mavenproject1`**
   (la que contiene `pom.xml`, no la carpeta superior).
3. Verificar el JDK: clic derecho en el proyecto → **Properties → Build → Compile →
   Java Platform** = JDK 11 (o JDK 8).
4. Clic derecho en el proyecto → **Clean and Build** → debe terminar en `BUILD SUCCESS`.
5. Ejecutar con el botón **Run** (la clase principal configurada es
   `com.mycompany.mavenproject1.Lanzador`).

Al iniciar se pregunta si se usará **Ventanas** o **Consola** (SIA-10).
En modo consola, el menú se usa desde la ventana *Output* de NetBeans.

> Si los acentos se ven mal en la consola de Windows: Properties → Run → VM Options:
> `-Dfile.encoding=UTF-8`

## Ejecución sin NetBeans (alternativa)

Desde la carpeta `mavenproject1`, con el JDK en el PATH:

- Windows: doble clic en `ejecutar_sin_maven.bat`
- Linux / macOS: `./ejecutar_sin_maven.sh`

## Datos (persistencia batch, SIA-11)

Los datos se **cargan al iniciar** y se **guardan al salir** en `mavenproject1/data/`
(archivos CSV, separador `;`, UTF-8, con fila de cabecera):

| Archivo | Columnas |
|---|---|
| `convenios.csv` | idConvenio;nombre;universidadSocia;pais;duracionMeses;carreraAsociada;requisitos |
| `estudiantes.csv` | rut;nombre;carrera;anioIngreso;estadoProceso |
| `tramites.csv` | idTramite;idConvenio;rutEstudiante;estado;observacion |
| `documentos.csv` | idTramite;tipoDocumento;nombreArchivo;fechaSubida |

- Los CSV se pueden editar a mano o con Excel. Si una línea tiene errores, se omite
  y el programa informa el archivo, la línea y el motivo; el resto se carga igual.
- Si la carpeta `data/` no existe, se cargan los **datos de ejemplo**
  (5 convenios, 8 estudiantes y 7 trámites en todos los estados).
- En cualquier momento: *Archivo → Restaurar datos de ejemplo* (o la opción 7 en consola).

## Estructura (patrón MVC)

```
mavenproject1/src/main/java/com/mycompany/mavenproject1/
├── Lanzador.java              Punto de entrada: carga datos y pregunta consola/ventanas
├── modelo/                    Estudiante, Convenio, Tramite, DocumentoSubido y enums
├── control/                   Control (lógica de negocio) y Validador
├── reportes/                  Reporte (abstracta) y sus 2 subclases
├── persistencia/              DataStore (CSV) y ExportadorExcel (.xlsx)
├── excepciones/               6 excepciones propias
└── vista/
    ├── consola/ConsoleApp.java
    └── ventana/               VentanaPrincipal, paneles, diálogos y gráfico
docs/uml/                      Diagramas UML (PNG, SVG y fuente PlantUML)
```

## Opcionales implementados

- **SIA-O1** Gráfico de barras de trámites por estado (Reportes).
- **SIA-O2** Exportación a planilla Excel `.xlsx` (Archivo → Exportar planilla Excel).
- **SIA-O3** Documentación Javadoc en todas las clases (NetBeans: clic derecho → *Generate Javadoc*).
- **SIA-O4** Patrón Modelo-Vista-Controlador (paquetes `modelo`, `control`, `vista`).
