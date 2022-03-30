DIR=$PWD/../..

rm -rf "./LabelPlusFX"

OUT="$DIR/out"
MODULES="$DIR/out/artifacts/lpfx_jar"
ICON="$DIR/images/icons/cat.icns"

jpackage --verbose --type app-image --app-version 2.3.0 --copyright "Meodinger Tech (C) 2022" --name LabelPlusFX --icon $ICON --dest . --module-path $MODULES --add-modules jdk.crypto.cryptoki --module lpfx/ink.meodinger.lpfx.LauncherKt