@echo off
echo ==================================================
echo   Online Recipe Sharing Platform - Compile & Run  
echo ==================================================

if not exist "target\classes" mkdir "target\classes"

echo [1/2] Compiling Java source files...
javac -cp "lib/*" -d target/classes src/main/java/com/recipeplatform/*.java src/main/java/com/recipeplatform/*/*.java

if %ERRORLEVEL% EQU 0 (
    echo [2/2] Launching Java Swing Application...
    java -cp "target/classes;lib/*" com.recipeplatform.Main
) else (
    echo [ERROR] Compilation failed. Please make sure Java JDK is installed on your computer.
    pause
)
