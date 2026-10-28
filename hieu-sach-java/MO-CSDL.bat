@echo off
cd /d "%~dp0"
echo Dung CHAY-WEB.bat bang Ctrl+C truoc khi mo CSDL.
echo JDBC URL: jdbc:h2:file:./data/hieusach
echo User: sa  - Password: de trong
echo Chi chay 1 chuong trinh mo CSDL tai mot thoi diem.
java -cp "lib\h2-2.5.252.jar" org.h2.tools.Console -web -webPort 8082 -browser
pause
