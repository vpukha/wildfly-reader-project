@ECHO off

SET mypath=%~dp0
cls

:start
ECHO.
ECHO 1. Register Simpleway WildFlyReader service
ECHO 2. Un-register Simpleway WildFlyReader service
set /p choice=What do you want to do?
if not '%choice%'=='' set choice=%choice:~0,1%
if '%choice%'=='1' goto install
if '%choice%'=='2' goto uninstall
ECHO "%choice%" is not valid please try again
ECHO.
goto start


:install
%mypath%wildfly-reader-service.exe install
goto end

:uninstall
%mypath%wildfly-reader-service.exe uninstall
goto end

:end
pause
exit 