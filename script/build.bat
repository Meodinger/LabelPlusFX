@echo off

set DIR=%~dp0..

set OUTPUT=%DIR%\out\LabelPlusFX

rd /S /Q "%OUTPUT%"

set OUT="%DIR%\out"
set MODULES="%DIR%\out\artifacts\lpfx_jar"
set ICON="%DIR%\images\icons\cat.ico"

javapackager -deploy -v -native image -outdir %OUT% --module-path %MODULES% --module lpfx/info.meodinger.lpfx.LauncherKt --strip-native-commands true -name LabelPlusFX -width 900 -height 600 -Bicon=%ICON%