#!/bin/bash

# Navigate to the Sources directory where the .java files are located
cd Sources

# Compile the Java code
# The -d option specifies the destination directory for the .class files
# The '.' specifies the current directory as the location of the .java files

# compiled .class files in the same directory as the .java files
javac -d . *.java



