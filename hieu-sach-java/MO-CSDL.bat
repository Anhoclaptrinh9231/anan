@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
if exist "data\START-MYSQL.bat" (
  call "data\START-MYSQL.bat"
  if errorlevel 1 goto failed
)
set "MYSQL_EXE=mysql.exe"
if exist "%ProgramFiles%\MySQL\MySQL Server 8.4\bin\mysql.exe" set "MYSQL_EXE=%ProgramFiles%\MySQL\MySQL Server 8.4\bin\mysql.exe"
if defined BOOKSTORE_MYSQL_BIN set "MYSQL_EXE=%BOOKSTORE_MYSQL_BIN%\mysql.exe"

echo Thu go SHOW TABLES; roi nhan Enter. Go exit de dong.
if defined BOOKSTORE_MYSQL_CONFIG (
  "%MYSQL_EXE%" --defaults-file="%BOOKSTORE_MYSQL_CONFIG%"
) else (
  "%MYSQL_EXE%" --host=127.0.0.1 --user=hieusach --password --default-character-set=utf8mb4 bookstore_v2
)
if errorlevel 1 goto failed
exit /b 0

:failed
echo Khong mo duoc MySQL. Xem BAT-DAU-O-DAY.md.
pause
exit /b 1
