@echo off
:: Nora Build Script — 支持 bundled / slim 两种模型打包 flavor
::
:: 用法:
::   build.bat               # 构建 bundledDebug（默认，内置模型）
::   build.bat bundled       # 构建 bundledDebug
::   build.bat slim          # 构建 slimDebug
::   build.bat bundledRelease # 构建 bundledRelease
::   build.bat slimRelease   # 构建 slimRelease
::
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "GRADLE_OPTS=-Xmx1536m"
set "ANDROID_HOME=C:\Users\28767\AppData\Local\Android\Sdk"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%PATH%"
cd /d "C:\Users\28767\WorkBuddy\local-agent"

set "BUILD_VARIANT=bundledDebug"
if "%~1"=="" goto build
if "%~1"=="bundled"    set "BUILD_VARIANT=bundledDebug"    & goto build
if "%~1"=="slim"       set "BUILD_VARIANT=slimDebug"        & goto build
if "%~1"=="release"    set "BUILD_VARIANT=bundledRelease"   & goto build
if "%~1"=="bundledRelease" set "BUILD_VARIANT=bundledRelease" & goto build
if "%~1"=="slimRelease"   set "BUILD_VARIANT=slimRelease"    & goto build
echo Unknown flavor: %~1
echo Usage: build.bat [bundled ^| slim]
exit /b 1

:build
echo [Nora Build] Variant: %BUILD_VARIANT%
gradlew.bat assemble%BUILD_VARIANT% 2>&1
echo BUILD_EXITCODE=%ERRORLEVEL%
