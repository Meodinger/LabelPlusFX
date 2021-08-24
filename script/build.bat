cd ..\out\artifacts\lpfx_jar

javapackager -deploy -v -native image -outdir packages --module-path . --module lpfx/info.meodinger.lpfx.LauncherKt --strip-native-commands true -name LabelPlusFX -width 900 -height 600 -Bicon=..\..\..\images\icons\cat.ico

cd ..\..\..\script