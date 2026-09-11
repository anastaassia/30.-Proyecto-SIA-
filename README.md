# Proyecto SIA - Sistema de Gestión de Intercambio Estudiantil

Este repositorio contiene el código fuente del **Proyecto SIA**, un sistema desarrollado en Java diseñado para administrar programas de intercambio estudiantil. La aplicación facilita el seguimiento de postulaciones, gestión de colecciones de datos y control de requisitos para estudiantes.

## Características Principales

* **Gestión de Entidades:** Administración completa de estudiantes (`Estudiante.java`), convenios institucionales (`Convenio.java`) y trámites (`Tramite.java`).
* **Control de Documentos:** Sistema integrado para el registro de archivos subidos (`DocumentoSubido.java`), el cual incorpora manejo de excepciones personalizadas, como `DocumentoDuplicadoException`, para proteger la integridad de las postulaciones y evitar redundancias.
* **Interfaz Gráfica (GUI):** Incluye una aplicación visual dividida en paneles modulares (como `PanelGestionEstudiantes`, `PanelGestionConvenios`, `PanelTramites` y `PanelRequisitos`) centralizados en una `VentanaPrincipal`.
* **Persistencia de Datos:** La información del sistema se guarda y recupera de manera local mediante archivos CSV almacenados en el directorio `data/` (`estudiantes.csv`, `convenios.csv`, `documentos.csv`, `tramites.csv`) a través del gestor `DataStore.java`.
* **Alternativa de Consola:** El sistema también cuenta con una interfaz de línea de comandos a través de `ConsoleApp.java`.

## Estructura del Proyecto

El proyecto utiliza **Maven** para la gestión de dependencias y la construcción del software. La estructura de directorios es la siguiente:

```text
30.-Proyecto-SIA--main/
├── mavenproject1/
│   ├── pom.xml                  # Archivo de configuración de Maven
│   ├── data/                    # Archivos de persistencia de datos
│   │   ├── convenios.csv        
│   │   ├── documentos.csv      
│   │   ├── estudiantes.csv     
│   │   └── tramites.csv        
│   └── src/main/java/com/mycompany/mavenproject1/
│       ├── Main.java / ConsoleApp.java      # Puntos de ejecución
│       ├── Control.java / DataStore.java    # Lógica de controladores y datos
│       ├── Modelos                          # Estudiante, Convenio, Tramite, etc.
│       ├── Excepciones                      # EstudianteNoEncontradoException, etc.
│       └── Componentes Visuales             # VentanaPrincipal y clases Panel*.java
```

## Requisitos Previos

* **Java Development Kit (JDK)** instalado en el sistema.
* **Apache Maven** para la compilación y gestión del proyecto.

## Instalación y Ejecución

1. Clona este repositorio en tu máquina local.
2. Abre una terminal y navega hasta el directorio principal del proyecto Maven (`30.-Proyecto-SIA--main/mavenproject1/`).
3. Compila el proyecto ejecutando:
   ```bash
   mvn clean install
   ```
4. Para iniciar la aplicación, ejecuta la clase `Main.java` (para la interfaz gráfica) o la clase `ConsoleApp.java` (para interactuar mediante la terminal).
