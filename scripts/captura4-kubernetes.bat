@echo off
rem Captura 4 (Kubernetes): ejecuta captura4-kubernetes.ps1 y guarda la evidencia en docs\evidencias
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0captura4-kubernetes.ps1"
echo.
pause
