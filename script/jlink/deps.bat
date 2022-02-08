@echo off

rem Print deps for jlink
jdeps --module-path .\jar --multi-release 11 --print-module-deps --ignore-missing-deps .\jar\lpfx .\jar\*.jar