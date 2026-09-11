# Sistema de Gestión de Intercambio Estudiantil
Gestión de Programas de Intercambio Estudiantil: Administración de convenios internacionales, registro de estudiantes, y manejo de trámites y documentación.

## Requisitos
- **Java JDK 11+** (recomendado 17)
- **Apache Maven 3.8+**
- (Opcional) **NetBeans / IntelliJ / VS Code** para ejecución desde IDE

Verifica versiones:
~~~
java -version
mvn -v
~~~

## Estructura del proyecto
- **Paquete principal:** `com.mycompany.mavenproject3`
- **Clase de entrada:** `com.mycompany.mavenproject3.Main`
- **Persistencia:** En memoria (no usa BD). Al cerrar el programa, se pierde el estado.

Estructura de carpetas esperada:
~~~
src/
 └─ main/
     └─ java/
         └─ com/
             └─ mycompany/
                 └─ intercambioEstudiantil/
                     ├─ Main.java
                     ├─ Control.java
                     ├─ Estudiante.java
                     ├─ Convenio.java
                     ├─ Tramite.java
                     ├─ DocumentoSubido.java
                     └─ TipoDocumento.java
pom.xml
~~~

## Instalación
1. Clona o descarga el repositorio.
2. Abre una terminal en la carpeta del proyecto (donde está `pom.xml`).
3. (Si hace falta) añade o revisa los plugins en `pom.xml`:

~~~xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.11.0</version>
      <configuration>
        <source>11</source>
        <target>11</target>
      </configuration>
    </plugin>

    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>exec-maven-plugin</artifactId>
      <version>3.1.0</version>
      <configuration>
        <mainClass>com.mycompany.mavenproject3.Main</mainClass>
      </configuration>
    </plugin>
  </plugins>
</build>
~~~

> **Tip**: Si prefieres un `.jar` ejecutable, agrega `maven-shade-plugin` para empaquetar todo y usar `java -jar`.

## Compilación
~~~
mvn clean compile -DskipTests
~~~

## Ejecución (línea de comandos)

### Opción A — Maven Exec Plugin (recomendada)
~~~
mvn exec:java -Dexec.mainClass=com.mycompany.mavenproject3.Main
~~~
Si en el `pom.xml` ya configuraste `<mainClass>`, basta con:
~~~
mvn exec:java
~~~

### Opción B — Desde IDE
- **NetBeans**: clic derecho sobre `Main.java` → **Run File**  
  (o **Project Properties → Run → Main Class** y setea `com.mycompany.mavenproject3.Main`).
- **IntelliJ/VS Code**: abre `Main.java` y ejecuta la clase `Main` (botón ▶).

## Uso (menú principal)
Al iniciar, verás un menú similar a:
~~~
1) Registrar estudiante
2) Crear trámite de postulación
3) Subir documento a trámite
4) Ver estado de trámite
5) Listar convenios y trámites
6) Configurar requisitos de un convenio
0) Salir
~~~

### Tipos de documento (según `TipoDocumento`)
~~~
CERT_NACIMIENTO, CERT_ALUMNO_REGULAR, PASAPORTE, CERTIFICADO_NOTAS,
CARTA_MOTIVACION, CV, CERTIFICADO_IDIOMA
~~~

### Estados de trámite (referencia)
~~~
EN_PROCESO, COMPLETO
~~~

## Problemas comunes y soluciones

**1) Error: `Could not find or load main class com.mycompany.mavenproject3.Main`**  
- **Causa:** Ruta/paquete no coincide con la estructura real, o `exec-maven-plugin` no apunta a la clase correcta.  
- **Solución:**
  - Verifica que el **package** en `Main.java` sea exactamente `package com.mycompany.mavenproject3;`
  - Verifica la carpeta: `src/main/java/com/mycompany/intercambioEstudiantil/Main.java`
  - Ejecuta:
    ~~~
    mvn -q -Dexec.cleanupDaemonThreads=false exec:java -Dexec.mainClass=com.mycompany.mavenproject3.Main
    ~~~

**2) `mvn` o `java` no reconocidos**  
- **Solución:** Asegúrate de tener **JAVA_HOME** y **Maven** en el `PATH`. Reinicia la terminal tras instalar.

**3) Caracteres raros (tildes/ñ) en Windows**  
- **PowerShell:**
  ~~~powershell
  chcp 65001
  $env:JAVA_TOOL_OPTIONS=" -Dfile.encoding=UTF-8"
  ~~~

## Licencia
Proyecto académico/MVP para fines educativos.
