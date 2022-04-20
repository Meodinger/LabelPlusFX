@echo off

rem Remove existing dirs & files
rd /S /Q "jar"
mkdir "jar"

rem Copy new files
copy "..\..\target\build\*.jar" ".\jar\"
