#!/bin/bash
echo "=================================================="
echo "  Online Recipe Sharing Platform - Compile & Run  "
echo "=================================================="

# Create target directory
mkdir -p target/classes

echo "[1/2] Compiling Java source files..."
javac -cp "lib/*" -d target/classes $(find src/main/java -name "*.java")

if [ $? -eq 0 ]; then
    echo "[2/2] Launching Java Swing Application..."
    java -cp "target/classes:lib/*" com.recipeplatform.Main
else
    echo "[ERROR] Compilation failed. Please check Java errors."
fi
