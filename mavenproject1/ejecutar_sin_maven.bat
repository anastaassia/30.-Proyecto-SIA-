@echo off
REM Alternativa a NetBeans/Maven: compila y ejecuta solo con el JDK (8 u 11).
REM Ejecutar con doble clic o desde una consola ubicada en esta carpeta.
chcp 65001 > nul
if exist out rmdir /s /q out
mkdir out
dir /s /b src\main\java\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
if errorlevel 1 (
  echo Error de compilacion. Verifique que el JDK este instalado y en el PATH.
  del fuentes.txt
  pause
  exit /b 1
)
del fuentes.txt
java -Dfile.encoding=UTF-8 -cp out com.mycompany.mavenproject1.Lanzador
pause
