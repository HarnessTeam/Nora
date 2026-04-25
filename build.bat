@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "GRADLE_OPTS=-Xmx1536m"
set "ANDROID_HOME=C:\Users\28767\AppData\Local\Android\Sdk"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%PATH%"
cd /d "C:\Users\28767\WorkBuddy\local-agent"
gradlew.bat assembleDebug 2>&1
echo BUILD_EXITCODE=%ERRORLEVEL%
