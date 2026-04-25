$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot'
$env:ANDROID_HOME = 'C:\Users\28767\AppData\Local\Android\Sdk'
$env:GRADLE_OPTS = '-Xmx1536m'
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator;$env:PATH"
Set-Location 'C:\Users\28767\WorkBuddy\local-agent'
$output = & .\gradlew.bat testSlimDebugUnitTest 2>&1
$output | Select-Object -Last 50
