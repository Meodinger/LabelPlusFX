DIR=$PWD/../..

rm -rf "$DIR/out/LabelPlusFX"

OUT="$DIR/out"
MODULES="$DIR/out/artifacts/lpfx_jar"
ICON="$DIR/images/icons/cat.icns"

jpackage --verbose --type app-image --name LabelPlusFX --icon $ICON --dest . --module-path $MODULES --module lpfx/ink.meodinger.lpfx.LauncherKt