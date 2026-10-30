@echo off
set DIRNAME=%~dp0
if exist "%DIRNAME%..\tools\maven\bin\mvn.cmd" (
    "%DIRNAME%..\tools\maven\bin\mvn.cmd" %*
) else if exist "%DIRNAME%tools\maven\bin\mvn.cmd" (
    "%DIRNAME%tools\maven\bin\mvn.cmd" %*
) else (
    mvn %*
)
