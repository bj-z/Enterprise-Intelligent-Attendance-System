# 企业智能考勤与绩效管理系统

> Enterprise Intelligent Attendance & Performance System

基于前后端分离架构的现代化考勤管理系统，融合 DeepSeek 大语言模型实现智能考勤异常预警与多维度绩效分析，支持 Docker Compose 一键部署。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3 + Vite + Element Plus + ECharts + Axios |
| **后端** | Spring Boot 3.2 + Spring Security + Spring Data JPA |
| **数据库** | MySQL 8.0 |
| **认证** | JWT + BCrypt |
| **AI** | DeepSeek Chat API（异常预警 + 绩效评语生成） |
| **API 文档** | Knife4j (Swagger) |
| **部署** | Docker Compose |
| **监控** | Prometheus + Grafana + SkyWalking + ELK |

---

## 项目结构

```
├── attendance_management/    # Vue 3 前端项目
│   ├── src/views/            # 页面组件（12个功能页面）
│   ├── src/api/              # API 接口封装
│   └── src/router/           # 路由配置
├── server/                   # Spring Boot 后端项目
│   └── src/main/java/com/attendance/
│       ├── controller/       # REST 控制器
│       ├── service/          # 业务逻辑层
│       ├── domain/           # JPA 实体类（12张表）
│       ├── repository/       # 数据访问层
│       ├── config/           # 安全/跨域/JWT 配置
│       └── util/             # 工具类（JWT/AES）
├── documents/                # 项目文档
├── docker-compose.yml        # 本地开发部署
├── docker-compose-ecs.yml    # ECS 单机部署（含 MySQL）
└── 一键部署脚本
```

---

## 功能模块

| 模块 | 说明 |
|------|------|
| 员工管理 | 50 名员工、8 个部门的增删改查与出勤率统计 |
| 考勤记录 | 每日打卡记录、迟到/早退/缺卡自动判定 |
| 请假审批 | 请假申请提交与多级审批流程（年假/事假/病假/调休/婚假） |
| AI 异常预警 | DeepSeek 自动分析考勤异常并生成风险等级与改进建议 |
| AI 绩效分析 | 综合出勤率 + 任务得分 + 协作度，自动评分与 AI 评语 |
| 时间要求 | 按部门配置上班打卡时间（周一至周五） |
| 仪表盘 | 出勤率、迟到/请假/预警统计、图表可视化 |
| 用户管理 | 三类角色（系统管理员 / 人事主管 / 部门主管），RBAC 权限控制 |

---

## 快速开始

### 前置条件

- [Docker](https://www.docker.com/) & Docker Compose
- [Node.js](https://nodejs.org/) 20+（前端开发）
- [JDK 17](https://www.oracle.com/cn/java/technologies/downloads/) + [Maven](https://maven.apache.org/)（后端开发）
- [MySQL 8.0](https://dev.mysql.com/downloads/mysql/)（非 Docker 部署时需要）

### 方式一：Docker Compose 一键部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/bj-z/Enterprise-Intelligent-Attendance-System
cd demo

# 2. 启动所有服务（MySQL + 后端 + 前端）
docker compose -f docker-compose.yml up -d

# 3. 访问系统
# 前端：      http://localhost
# API 文档：  http://localhost:18080/swagger-ui/index.html
# 健康检查：  http://localhost:18080/actuator/health
```

首次启动时 MySQL 会自动执行 `server/src/main/resources/db/init.sql` 初始化数据库（12 张表 + 50 名员工 + 初始数据）。

### 方式二：手动运行（本地开发）

#### 1. 启动 MySQL 并初始化数据库

```bash
# 用 init.sql 创建数据库和初始数据
mysql -u root -p < server/src/main/resources/db/init.sql
```

#### 2. 启动后端

```bash
cd server
# 修改 src/main/resources/application.yml 中的数据库连接信息
mvn spring-boot:run
# 后端运行在 http://localhost:18080
```

#### 3. 启动前端

```bash
cd attendance_management
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

---

## 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `123456` | 系统管理员（全部权限） |
| `hr_manager` | `123456` | 人事主管 |
| `dept_leader` | `123456` | 部门主管 |

---

## 部署到服务器

### ECS 单机部署（含 MySQL）

```bash
# 1. 导出数据库备份（Windows 双击运行）
（Windows）一键导出数据库备份.bat

# 2. 构建并打包（Windows 双击运行）
（Windows）一键打包pack-for-ecs.bat

# 3. 上传 ecs-deploy-package.zip 到服务器，解压后执行
chmod +x ecs-deploy.sh
./ecs-deploy.sh
```

部署后访问 `http://<服务器公网IP>`，API 文档在 `http://<服务器IP>:18080/doc.html`。

---

## 数据库备份与恢复

### 导出备份（Windows）

```bash
# 双击运行
（Windows）一键导出数据库备份.bat
```

文件会生成到 `server/attendance_system_export.sql`。

### 恢复备份

```bash
mysql -u root -p attendance_system < server/attendance_system_export.sql
```

---

## API 文档

启动后端后访问：`http://localhost:18080/doc.html`

主要接口：
- `POST /api/auth/login` — 登录
- `GET /api/employees` — 员工列表（分页）
- `GET /api/attendance/records` — 考勤记录
- `POST /api/attendance/records` — 打卡
- `GET /api/leaves` — 请假列表
- `POST /api/leaves` — 提交请假
- `PUT /api/leaves/{id}/approve` — 审批请假
- `GET /api/alerts` — AI 异常预警列表
- `POST /api/alerts/analyze` — 触发 AI 分析
- `GET /api/performance` — AI 绩效分析
- `GET /api/dashboard` — 仪表盘统计数据

---


## License

本项目仅用于学习交流目的。
