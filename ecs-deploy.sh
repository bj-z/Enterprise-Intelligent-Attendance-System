#!/bin/bash
# ============================================
# 阿里云ECS一键部署脚本
# 前置条件：已通过 pack-for-ecs.bat 打包并上传到ECS
# 执行方式：
#   chmod +x ecs-deploy.sh
#   ./ecs-deploy.sh
# ============================================
set -e

echo "============================================"
echo "  考勤系统 - ECS一键部署"
echo "============================================"
echo ""

# ===== 1. 安装 Docker =====
if ! command -v docker &> /dev/null; then
    echo "[1/5] 安装 Docker..."
    curl -fsSL https://get.docker.com | bash -s docker
    systemctl enable docker
    systemctl start docker
    echo "  ✓ Docker 安装完成"
else
    echo "[1/5] Docker 已安装 ✓"
fi

# ===== 2. 安装 docker-compose =====
if docker compose version &> /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo "[2/5] 安装 docker-compose..."
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    COMPOSE_CMD="docker-compose"
fi
echo "[2/5] docker-compose 就绪 ✓"

# ===== 3. 配置防火墙 =====
echo "[3/5] 配置防火墙..."
# 阿里云安全组需要在控制台开放 80 和 18080 端口
if command -v firewall-cmd &> /dev/null; then
    firewall-cmd --add-port=80/tcp --permanent &> /dev/null || true
    firewall-cmd --add-port=18080/tcp --permanent &> /dev/null || true
    firewall-cmd --reload &> /dev/null || true
fi
echo "  请确保阿里云安全组已开放: 80, 18080 端口"
echo ""

# ===== 4. 停止旧容器并启动新服务 =====
echo "[4/5] 启动服务..."
$COMPOSE_CMD -f docker-compose-ecs.yml down 2>/dev/null || true
$COMPOSE_CMD -f docker-compose-ecs.yml up -d --build

# ===== 5. 等待并验证 =====
echo "[5/5] 等待服务就绪..."
echo "  MySQL 正在初始化数据库..."

# 等待后端启动
MAX_RETRIES=100
retry=0
while [ $retry -lt $MAX_RETRIES ]; do
    if curl -sf --connect-timeout 3 --max-time 5 http://localhost:18080/actuator/health > /dev/null 2>&1; then
        echo "  ✓ 后端启动成功"
        break
    fi
    retry=$((retry + 1))
    echo "  等待后端启动中... ($retry/$MAX_RETRIES)"
    sleep 3
done
if [ $retry -ge $MAX_RETRIES ]; then
    echo "  ✗ 后端启动超时，请检查日志: $COMPOSE_CMD -f docker-compose-ecs.yml logs backend"
    exit 1
fi

# 等待前端启动
retry=0
while [ $retry -lt 60 ]; do
    if curl -sf --connect-timeout 3 --max-time 5 http://localhost > /dev/null 2>&1; then
        echo "  ✓ 前端启动成功"
        break
    fi
    retry=$((retry + 1))
    sleep 2
done
if [ $retry -ge 60 ]; then
    echo "  ✗ 前端启动超时，请检查日志: $COMPOSE_CMD -f docker-compose-ecs.yml logs frontend"
    exit 1
fi

PUBLIC_IP=$(curl -s ifconfig.me)
echo ""
echo "============================================"
echo "  部署完成！"
echo ""
echo "  前端地址:    http://${PUBLIC_IP}"
echo "  API文档:     http://${PUBLIC_IP}:18080/doc.html"
echo "  健康检查:    http://${PUBLIC_IP}:18080/actuator/health"
echo ""
echo "  查看日志:    $COMPOSE_CMD -f docker-compose-ecs.yml logs -f"
echo "  重启服务:    $COMPOSE_CMD -f docker-compose-ecs.yml restart"
echo "  停止服务:    $COMPOSE_CMD -f docker-compose-ecs.yml down"
echo "============================================"
