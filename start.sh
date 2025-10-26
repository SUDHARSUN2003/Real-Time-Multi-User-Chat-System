#!/bin/bash
# Compile all Java files
javac -cp ".:mysql-connector-j-9.4.0.jar" *.java

# Run the server
java -cp ".:mysql-connector-j-9.4.0.jar" ChatServer
