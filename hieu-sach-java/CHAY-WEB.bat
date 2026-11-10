@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Can cai JDK 21 hoac moi hon. Xem BAT-DAU-O-DAY.md.
  pause
  exit /b 1
)
where javac >nul 2>nul
if errorlevel 1 (
  echo Can JDK day du co javac. Xem BAT-DAU-O-DAY.md.
  pause
  exit /b 1
)
call "%~dp0TAI-THU-VIEN.bat"
if errorlevel 1 goto failed
if exist "data\START-MYSQL.bat" (
  call "data\START-MYSQL.bat"
  if errorlevel 1 goto failed
)
if not exist build mkdir build
echo Dang bien dich Java...
javac -encoding UTF-8 --release 21 -cp "lib/*" -d build src\*.java
if errorlevel 1 goto failed
echo Giu cua so nay mo khi demo. Dung web bang Ctrl+C.
java -cp "build;lib/*" Bookstore
if errorlevel 1 goto failed
pause
exit /b 0

:failed
echo Khong khoi dong duoc web. Xem BAT-DAU-O-DAY.md.
pause
exit /b 1
