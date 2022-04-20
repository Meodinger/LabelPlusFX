@echo off

set DIR=%~dp0..\..
rd /S /Q ".\LabelPlusFX"

set MODULES="%DIR%\target\build"
set ICON="%DIR%\images\icons\cat.ico"

jpackage --verbose --type app-image --app-version 2.3.0 --copyright "Meodinger Tech (C) 2022" --name LabelPlusFX --icon %ICON% --dest . --module-path %MODULES% --add-modules jdk.crypto.cryptoki --module lpfx/ink.meodinger.lpfx.LauncherKt

echo:
echo All completed, remember to copy dlls!