@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "JAR=C:\Users\28767\.gradle\caches\9.1.0\transforms\9eb15c51dd0681590fc8cff653c0af92\workspace\transformed\navigation3-runtime-api.jar"
"%JAVA_HOME%\bin\javap" -public "androidx.navigation3.runtime.NavBackStack" -classpath "%JAR%"
echo ---
"%JAVA_HOME%\bin\javap" -public "androidx.navigation3.runtime.NavEntry" -classpath "%JAR%"
echo ---
"%JAVA_HOME%\bin\javap" -public "androidx.navigation3.runtime.NavKey" -classpath "%JAR%"
