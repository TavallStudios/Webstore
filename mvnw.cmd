@echo off
setlocal

set "BASE_DIR=%~dp0"
set "WRAPPER_DIR=%BASE_DIR%.mvn\wrapper"
set "MAVEN_VERSION=3.9.11"
set "ARCHIVE_NAME=apache-maven-%MAVEN_VERSION%-bin.zip"
set "ARCHIVE_PATH=%WRAPPER_DIR%\%ARCHIVE_NAME%"
set "MAVEN_HOME=%WRAPPER_DIR%\apache-maven-%MAVEN_VERSION%"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue';" ^
    "$archive='%ARCHIVE_PATH%';" ^
    "if (-not (Test-Path $archive)) { Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/%ARCHIVE_NAME%' -OutFile $archive; }" ^
    "$destination='%WRAPPER_DIR%';" ^
    "Expand-Archive -Path $archive -DestinationPath $destination -Force;"
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
