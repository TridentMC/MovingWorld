@echo off
mkdir project_libraries
set GRADLE_USER_HOME=%CD%\project_libraries
gradlew setupDecompWorkspace idea eclipse