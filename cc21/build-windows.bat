@echo off
chcp 65001 >nul
echo Сборка CheatClient...
call gradlew.bat build --no-daemon
if errorlevel 1 (
  echo.
  echo Сборка не удалась. Проверь, что установлен JDK 21: https://adoptium.net
  pause
  exit /b 1
)
if not exist "%APPDATA%\.minecraft\mods" mkdir "%APPDATA%\.minecraft\mods"
for %%f in (build\libs\cheatclient-*.jar) do (
  echo %%f | findstr /v /i "sources" >nul && copy /y "%%f" "%APPDATA%\.minecraft\mods\" >nul && echo Скопировано: %%f
)
echo Готово. Запускай Minecraft 1.21.11 с профилем Fabric.
pause
