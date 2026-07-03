@echo off
chcp 65001 >nul
echo ========================================
echo   启动考勤管理系统后端（开发模式）...
echo ========================================
set MAVEN_OPTS=-Dfile.encoding=UTF-8
mvn spring-boot:run
pause
