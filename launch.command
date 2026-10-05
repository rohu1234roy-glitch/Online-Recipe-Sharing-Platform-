#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"
mkdir -p target/classes
javac -cp "lib/*" -d target/classes $(find src/main/java -name "*.java")
java -cp "target/classes:lib/*" com.recipeplatform.Main
