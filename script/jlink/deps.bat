@echo off

rem Print deps for jlink
jdeps --module-path .\jar -quiet --multi-release 9 --print-module-deps --ignore-missing-deps .\jar\*.jar
