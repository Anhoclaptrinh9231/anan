@echo off
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 --release 21 -cp "lib/*" -d build src\Bookstore.java tests\IntegrationTest.java
if errorlevel 1 (
  pause
  exit /b 1
)
java -cp "build;lib/*" IntegrationTest
pause
