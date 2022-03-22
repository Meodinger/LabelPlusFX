@echo off

set DIR=%~dp0..\..
rd /S /Q ".\LabelPlusFX"

set OUT="%DIR%\out"
set MODULES="%DIR%\out\artifacts\lpfx_jar"
set ICON="%DIR%\images\icons\cat.ico"

jpackage --verbose --type app-image --name LabelPlusFX --icon %ICON% --dest . --module-path %MODULES% --add-modules jdk.crypto.cryptoki --module lpfx/ink.meodinger.lpfx.LauncherKt

echo "All completed, remember to copy dlls!"