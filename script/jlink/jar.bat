@echo off

rem Remove existing dirs & files
rd /S /Q "jar"
mkdir "jar"

rem Copy new files
copy "..\..\out\artifacts\lpfx_jar\*.jar" ".\jar\"

rem Extract main module
rem There is a bug with Java Sound SPI. If not extract, we cannot play ogg files.
mkdir ".\jar\lpfx"
tar -xf ".\jar\lpfx.jar" -C ".\jar\lpfx"
del /F /Q ".\jar\lpfx.jar"