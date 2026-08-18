@echo off
setlocal

:run
py "%~dp0generate_markdown.py"
echo.
echo Press R to run again, or any other key to exit.
powershell -NoProfile -Command "$k = $host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown'); if ($k.Character -eq 'r' -or $k.Character -eq 'R') { exit 1 } else { exit 0 }"
if errorlevel 1 goto run

endlocal
