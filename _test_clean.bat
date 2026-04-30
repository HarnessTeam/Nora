@echo off
cd /d C:\Users\28767\WorkBuddy\local-agent
set GRADLE_OPTS=-Xmx1536m
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set ANDROID_HOME=C:\Users\28767\AppData\Local\Android\Sdk
echo Running clean + connectedSlimDebugAndroidTest...
call gradlew.bat clean connectedSlimDebugAndroidTest --no-daemon > test_clean.txt 2>&1
echo Exit code: %ERRORLEVEL%
