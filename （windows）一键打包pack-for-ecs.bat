@echo off
chcp 65001 >nul
echo ============================================
echo   考勤系统 - ECS部署打包脚本
echo   在Windows本地构建JAR和前端，打包上传到ECS
echo ============================================
echo.

set PACK_DIR=ecs-deploy-package

echo [1/6] 清理旧打包目录...
if exist %PACK_DIR% rmdir /s /q %PACK_DIR%
mkdir %PACK_DIR%
mkdir %PACK_DIR%\server
mkdir %PACK_DIR%\attendance_management

echo.
echo [2/6] 构建后端 JAR（Maven）...
cd server
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo 后端构建失败！请检查 Maven 是否已安装。
    cd ..
    pause
    exit /b 1
)
cd ..
echo    ✓ 后端 JAR 构建成功

echo.
echo [3/6] 构建前端 dist（npm）...
cd attendance_management
call npm run build
if %ERRORLEVEL% NEQ 0 (
    echo 前端构建失败！
    cd ..
    pause
    exit /b 1
)
cd ..
echo    ✓ 前端构建成功

echo.
echo [4/6] 复制部署文件到打包目录...
REM 后端（保持 target 目录结构，与 Dockerfile-ecs 中 COPY target/*.jar 一致）
mkdir %PACK_DIR%\server\target
copy server\target\*.jar %PACK_DIR%\server\target\ >nul
copy server\Dockerfile-ecs %PACK_DIR%\server\ >nul

REM 前端
xcopy /e /i attendance_management\dist %PACK_DIR%\attendance_management\dist >nul
copy attendance_management\Dockerfile-ecs %PACK_DIR%\attendance_management\ >nul
copy attendance_management\nginx-ecs.conf %PACK_DIR%\attendance_management\ >nul

REM docker-compose
copy docker-compose-ecs.yml %PACK_DIR%\ >nul

REM 数据库备份（仅存在时才打包）
if exist server\attendance_system_export.sql (
    echo    ✓ 检测到数据库备份，正在打包...
    copy server\attendance_system_export.sql %PACK_DIR%\ >nul
    echo    ✓ 数据库备份已打包
) else (
    echo    - 未检测到数据库备份，跳过
)

copy ecs-deploy.sh %PACK_DIR%\ >nul

echo    ✓ 文件复制完成

echo.
echo [5/6] 打包为 ZIP...
powershell -Command "Compress-Archive -Path '%PACK_DIR%' -DestinationPath '%PACK_DIR%.zip' -Force"
echo    ✓ ZIP 已生成: %PACK_DIR%.zip

echo.
echo [6/6] 打包内容一览...
dir /s /b %PACK_DIR%

echo.
echo ============================================
echo   打包完成！文件：%PACK_DIR%.zip
echo.
echo   下一步：
echo      1. 上传 ZIP 到 ECS
echo      2. 把文件解压出来：
echo      unzip %PACK_DIR%.zip
echo      3. 进入解压后的文件夹内：
echo      cd %PACK_DIR%
echo      4. 修改启动脚本权限：
echo      chmod +x ecs-deploy.sh
echo      5. 启动脚本
echo      ./ecs-deploy.sh
echo ============================================
pause
