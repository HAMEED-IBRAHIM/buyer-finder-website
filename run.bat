@echo off
REM BuyerFinder - One-click setup and run script
REM This script finds Java, sets JAVA_HOME, and starts the Spring Boot app

echo ========================================
echo  BuyerFinder - Setup and Launch Script
echo ========================================
echo.

REM Try to find Java in common install locations
set JAVA_FOUND=0

IF EXIST "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.1+1\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.1+1
    set JAVA_FOUND=1
    goto :found
)

FOR /D %%i IN ("C:\Program Files\Eclipse Adoptium\jdk-17*") DO (
    IF EXIST "%%i\bin\java.exe" (
        set JAVA_HOME=%%i
        set JAVA_FOUND=1
        goto :found
    )
)

FOR /D %%i IN ("C:\Program Files\Microsoft\jdk-17*") DO (
    IF EXIST "%%i\bin\java.exe" (
        set JAVA_HOME=%%i
        set JAVA_FOUND=1
        goto :found
    )
)

FOR /D %%i IN ("C:\Program Files\Java\jdk-17*") DO (
    IF EXIST "%%i\bin\java.exe" (
        set JAVA_HOME=%%i
        set JAVA_FOUND=1
        goto :found
    )
)

REM Try registry
FOR /F "tokens=2*" %%a IN ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v JavaHome 2^>nul') DO (
    set JAVA_HOME=%%b
    set JAVA_FOUND=1
    goto :found
)

:found
IF "%JAVA_FOUND%"=="0" (
    echo [ERROR] Java not found! Please install Java 17 from:
    echo         https://adoptium.net/temurin/releases/?version=17
    echo.
    pause
    exit /b 1
)

echo [OK] Found Java at: %JAVA_HOME%
set PATH=%JAVA_HOME%\bin;%PATH%

"%JAVA_HOME%\bin\java.exe" -version
echo.
echo [OK] Starting BuyerFinder on http://localhost:8080 ...
echo.

cd /d "%~dp0"
call mvnw.cmd spring-boot:run
