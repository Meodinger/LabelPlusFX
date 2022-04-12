@echo off

rem Remove existing dirs & files
rd /S /Q "jar"
mkdir "jar"

rem Copy new files
copy "..\..\out\artifacts\lpfx_jar\*.jar" ".\jar\"
