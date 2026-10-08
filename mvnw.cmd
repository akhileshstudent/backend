@echo off
setlocal
set "MAVEN_VERSION=3.9.11"
if "%MAVEN_USER_HOME%"=="" set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "WRAPPER_ROOT=%MAVEN_USER_HOME%\wrapper\dists\hosteldekho-maven-%MAVEN_VERSION%"
set "MAVEN_HOME=%WRAPPER_ROOT%\apache-maven-%MAVEN_VERSION%"
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%WRAPPER_ROOT%" mkdir "%WRAPPER_ROOT%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $u='https://dlcdn.apache.org/maven/maven-%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest -Uri $u -OutFile '%WRAPPER_ROOT%\maven.zip'; Expand-Archive -Path '%WRAPPER_ROOT%\maven.zip' -DestinationPath '%WRAPPER_ROOT%' -Force; Remove-Item '%WRAPPER_ROOT%\maven.zip'"
  if errorlevel 1 exit /b 1
)
call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
