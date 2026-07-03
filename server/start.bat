@echo off
chcp 65001 >nul
title 考勤管理系统后端
echo ========================================
echo   启动考勤管理系统后端服务...
echo ========================================
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar target\attendance-server-1.0.0.jar
pause
