@echo off
cd /d "%~dp0"
java -jar frontend-desktop.jar
if errorlevel 1 pause
