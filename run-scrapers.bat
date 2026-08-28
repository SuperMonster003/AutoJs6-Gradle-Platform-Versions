@ECHO OFF
SETLOCAL ENABLEEXTENSIONS

PUSHD "%~dp0.utils" >NUL
IF ERRORLEVEL 1 (
  ECHO Failed to enter the scraper directory: "%~dp0.utils"
  EXIT /B 1
)

WHERE node >NUL 2>NUL
IF ERRORLEVEL 1 (
  ECHO Node.js was not found on PATH. Install Node.js 20.18.1 or newer and try again.
  POPD
  EXIT /B 1
)

WHERE npm >NUL 2>NUL
IF ERRORLEVEL 1 (
  ECHO npm was not found on PATH. Install npm and try again.
  POPD
  EXIT /B 1
)

IF NOT EXIST "node_modules\cheerio\package.json" (
  ECHO Installing scraper dependencies...
  CALL npm ci --no-audit --no-fund
  IF ERRORLEVEL 1 (
    ECHO Failed to install scraper dependencies.
    POPD
    EXIT /B 1
  )
  ECHO.
)

:RUN
CALL npm run update-data
SET "SCRAPER_EXIT_CODE=%ERRORLEVEL%"
ECHO.

IF NOT "%SCRAPER_EXIT_CODE%"=="0" (
  ECHO The scraper suite exited with code %SCRAPER_EXIT_CODE%.
  ECHO.
)

ECHO Press [R] to rerun, [Shift+R] to clear the screen then rerun,
ECHO or [ESC]/[Enter]/[Space] to exit...
PowerShell -NoLogo -NoProfile -Command ^
  "$ErrorActionPreference='Stop';" ^
  "while($true){" ^
  "  $k=$Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown');" ^
  "  if($k.VirtualKeyCode -eq 27 -or $k.VirtualKeyCode -eq 13 -or $k.Character -eq ' '){ exit 1 }" ^
  "  elseif($k.VirtualKeyCode -eq 82){" ^
  "    $isShift = ($k.ControlKeyState -band 0x0010) -ne 0 -or ($k.ControlKeyState -band 0x0080) -ne 0;" ^
  "    if($isShift){ exit 2 } else { exit 0 }" ^
  "  }" ^
  "}"

IF ERRORLEVEL 2 GOTO SHIFT_R
IF ERRORLEVEL 1 GOTO EXIT

ECHO.
GOTO RUN

:SHIFT_R
CLS
ECHO.
GOTO RUN

:EXIT
ECHO.
POPD
EXIT /B %SCRAPER_EXIT_CODE%
