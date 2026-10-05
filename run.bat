@echo off
echo ==================================================
echo   Online Recipe Sharing Platform - Compile & Run  
echo ==================================================

if not exist "target\classes" mkdir "target\classes"

echo [1/2] Compiling Java source files...
dir /s /b src\main\java\*.java > sources.txt
javac -cp "lib/*" -d target\classes @sources.txt
del sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [2/2] Launching Java Swing Application...
    java -cp "target\classes;lib\*" com.recipeplatform.Main
) else (
    echo [ERROR] Compilation failed. Please check Java errors.
    pause
)
