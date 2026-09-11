# Sistema de Gestión de Programas de Intercambio Estudiantil

Proyecto SIA — INF2236 Programación Avanzada — 2026-1

Sistema que gestiona las postulaciones de estudiantes a convenios de intercambio
académico entre universidades: registro de estudiantes, administración de
convenios, inicio de trámites de postulación, carga de documentos de respaldo y
validación automática de requisitos.

---

## Requisitos

- **Oracle JDK 11 u Oracle JDK 8** (el proyecto compila con ambos)
- **NetBeans 21 o inferior**, o Eclipse
- Maven (viene incluido en NetBeans)

---

## Instalación

1. Descomprimir el proyecto (o clonar el repositorio).
2. Abrir NetBeans → **File → Open Project**.
3. Seleccionar la carpeta **`mavenproject1`**, que es la que contiene el
   `pom.xml`. No seleccionar la carpeta superior del repositorio.
4. Click derecho sobre el proyecto → **Clean and Build**.
   Debe finalizar con `BUILD SUCCESS`.

---

## Ejecución

El sistema tiene un punto de entrada único que pregunta el modo de ejecución
(requisito SIA-10):

- Presionar el botón verde, o
- Click derecho sobre `Lanzador.java` → **Run File**

Aparece un diálogo con dos opciones:

| Opción | Descripción |
|---|---|
| **Ventana (GUI)** | Interfaz Swing con pestañas |
| **Consola** | Menú numerado en la ventana Output |

También se puede ejecutar cada interfaz directamente con **Run File** sobre
`Main.java` (gráfica) o `ConsoleApp.java` (consola).

### Nota sobre acentos en Windows

Si en la consola los acentos aparecen mal (`Postulaci�n`), ir a click derecho
sobre el proyecto → **Properties → Run → VM Options** y escribir:

```
-Dfile.encoding=UTF-8
```

---

## Datos

La persistencia usa cuatro archivos CSV en la carpeta `data/`, dentro de
`mavenproject1`:

| Archivo | Contenido |
|---|---|
| `convenios.csv` | Convenios y sus requisitos documentales |
| `estudiantes.csv` | Estudiantes registrados |
| `tramites.csv` | Trámites de postulación |
| `documentos.csv` | Documentos subidos a cada trámite |

La carga es batch al iniciar la aplicación y el guardado se realiza al salir.

Si la carpeta `data/` no existe, el sistema la crea automáticamente y carga un
conjunto de datos iniciales (2 estudiantes, 2 convenios y 1 trámite) que
permiten probar todas las funcionalidades.

---

## Estructura del proyecto

```
mavenproject1/
├── pom.xml
├── data/                     Archivos CSV de persistencia
└── src/main/java/com/mycompany/mavenproject1/
    ├── Lanzador.java         Punto de entrada (selector consola/ventana)
    ├── Main.java             Ventana principal (Swing)
    ├── ConsoleApp.java       Interfaz de consola
    ├── Control.java          Lógica de negocio
    ├── DataStore.java        Persistencia CSV
    ├── Convenio.java         Modelo de dominio
    ├── Estudiante.java
    ├── Tramite.java
    ├── DocumentoSubido.java
    ├── TipoDocumento.java    Enumerado
    ├── Panel*.java           Paneles de la interfaz gráfica
    └── *Exception.java       Excepciones propias
```

---

## Integrantes

- Anastasia Perez
- Alejandro Lanas
- Nicolas Echeverria
