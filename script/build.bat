@echo off

set DIR=%~dp0..
rd /S /Q "%DIR%\out\LabelPlusFX"
rd /S /Q "%DIR%\out\*.zip"

set OUT="%DIR%\out"
set MODULES="%DIR%\out\artifacts\lpfx_jar"
set ICON="%DIR%\images\icons\cat.ico"

jpackage --verbose --type app-image --name LabelPlusFX --icon %ICON% --dest %OUT% --module-path %MODULES% --module lpfx/info.meodinger.lpfx.LauncherKt