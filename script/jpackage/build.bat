@echo off

set DIR=%~dp0..\..
rd /S /Q ".\LabelPlusFX"

set MODULES="%DIR%\target\build"
set ICON="%DIR%\images\icons\cat.ico"

jpackage --verbose --type app-image --app-version 2.3.3 --copyright "Meodinger Tech (C) 2022" --name LabelPlusFX --icon %ICON% --dest . --module-path %MODULES% --add-modules lpfx,jdk.crypto.cryptoki --module lpfx/ink.meodinger.lpfx.LauncherKt  --java-options "-Dprism.maxvram=2G"

echo:
echo All completed, remember to copy dlls!
