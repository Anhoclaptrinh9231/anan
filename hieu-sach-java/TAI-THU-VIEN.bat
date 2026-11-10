@echo off
setlocal
cd /d "%~dp0"
if not exist lib mkdir lib
call :download com/mysql/mysql-connector-j/26.10.0 mysql-connector-j-26.10.0.jar cef70caf532833c9ab8f41f74773ce22c08bf503fad215c9c2ee1e9db87e759e || exit /b 1
call :download com/google/code/gson/gson/2.13.2 gson-2.13.2.jar dd0ce1b55a3ed2080cb70f9c655850cda86c206862310009dcb5e5c95265a5e0 || exit /b 1
exit /b 0

:download
if exist "lib\%~2" exit /b 0
echo Dang tai %~2...
powershell.exe -NoProfile -Command ^
  "$ErrorActionPreference='Stop'; $file='lib/%~2'; $temp=$file+'.'+[guid]::NewGuid()+'.tmp';" ^
  "try { curl.exe -fsSL --retry 2 -o $temp 'https://repo.maven.apache.org/maven2/%~1/%~2';" ^
  "if ($LASTEXITCODE -ne 0) { throw 'Tai thu vien that bai.' }" ^
  "$stream=[IO.File]::OpenRead((Resolve-Path -LiteralPath $temp).Path);" ^
  "try { $hash=[BitConverter]::ToString([Security.Cryptography.SHA256]::Create().ComputeHash($stream)).Replace('-','') } finally { $stream.Dispose() };" ^
  "if ($hash -ne '%~3') { throw 'Thu vien tai ve sai ma SHA-256.' }" ^
  "Move-Item -LiteralPath $temp -Destination $file -Force } finally { if (Test-Path -LiteralPath $temp) { Remove-Item -LiteralPath $temp } }"
exit /b %ERRORLEVEL%
