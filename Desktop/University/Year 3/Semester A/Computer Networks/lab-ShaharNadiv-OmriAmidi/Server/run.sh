#!/bin/bash

# Run the Java application
# The -cp option specifies the classpath to the .class files
# Here, we assume that the .class files are in the Sources directory
# WebServer is the class with the main method.

java -cp Sources WebServer
