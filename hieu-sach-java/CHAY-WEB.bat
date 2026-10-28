@echo off
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
if not exist "lib\h2-2.5.252.jar" (
  echo Thieu thu vien lib\h2-2.5.252.jar. Hay copy ca thu muc du an.
  pause
  exit /b 1
)
if not exist build mkdir build
echo Dang bien dich Java...
javac -encoding UTF-8 --release 21 -cp "lib/*" -d build src\Bookstore.java
if errorlevel 1 (
  pause
  exit /b 1
)
echo Mo trinh duyet tai http://localhost:8080
echo Giu cua so nay mo khi demo. Dung web bang Ctrl+C.
java -cp "build;lib/*" Bookstore
pause
