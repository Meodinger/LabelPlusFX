@echo off

echo:
echo --------------------
echo This bundle does not intent to be released. It should only be used to debug!
echo --------------------
echo:

rd /S /Q "runtime"

CALL copy.bat

rem Add jdk.crypto.cryptoki to support EC cipher suite. This will fix the GitHub api and Neko-Dict fetch issues.
FOR /F "delims=" %%i IN ('call deps.bat') DO jlink -v --module-path %JAVA_HOME%\jmods --add-modules %%i,jdk.crypto.cryptoki --no-man-pages --no-header-files --strip-debug --output runtime

echo:
echo All completed, remember to copy dlls!

