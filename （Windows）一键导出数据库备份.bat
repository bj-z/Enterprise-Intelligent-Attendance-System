@echo off
chcp 65001 >nul
echo ============================================
echo   考勤系统 - 导出数据库备份
echo ============================================
echo.

echo [1/1] 导出 attendance_system 数据库...
mysqldump -u root -p123456 --default-character-set=utf8mb4 --routines --triggers --single-transaction --databases --result-file=server\attendance_system_export.sql attendance_system

if %ERRORLEVEL% NEQ 0 (
    echo 导出失败！请检查 MySQL 是否正在运行。
    pause
    exit /b 1
)

echo.
echo ============================================
echo   导出成功！
echo   文件: server\attendance_system_export.sql
echo ============================================
pause
