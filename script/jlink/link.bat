@echo off

rd /S /Q "runtime"

FOR /F "delims=" %%i IN ('call deps.bat') DO jlink -v --module-path %JAVA_HOME%\jmods --add-modules %%i --no-man-pages --no-header-files --strip-debug --output runtime

CALL jar.bat