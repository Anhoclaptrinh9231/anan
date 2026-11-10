@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
call "%~dp0TAI-THU-VIEN.bat"
if errorlevel 1 goto failed
if exist "data\START-MYSQL.bat" (
  call "data\START-MYSQL.bat"
  if errorlevel 1 goto failed
)
if not exist build mkdir build
javac -encoding UTF-8 --release 21 -cp "lib/*" -d build src\*.java tests\*.java
if errorlevel 1 goto failed
java -cp "build;lib/*" MysqlTests
if errorlevel 1 goto failed
pause
exit /b 0

:failed
echo Kiem tra that bai. Xem BAT-DAU-O-DAY.md.
pause
exit /b 1
