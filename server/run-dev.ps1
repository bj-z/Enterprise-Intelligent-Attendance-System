Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动考勤管理系统后端（开发模式）..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 强制 PowerShell 控制台使用 UTF-8
$OutputEncoding = [Console]::OutputEncoding = [Console]::InputEncoding = [System.Text.Encoding]::UTF8
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8"

chcp 65001 | Out-Null

mvn spring-boot:run
pause
