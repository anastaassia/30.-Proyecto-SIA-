#!/bin/sh
# Alternativa a NetBeans/Maven: compila y ejecuta solo con el JDK (8 u 11).
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java') || exit 1
java -Dfile.encoding=UTF-8 -cp out com.mycompany.mavenproject1.Lanzador
