﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿-- =====================================================
-- 企业员工考勤管理系统 - 数据库初始化脚本 (MySQL)
-- 使用方式：mysql -u root -p --default-character-set=utf8mb4 < init.sql
-- 设计说明：
--   1. 至少8张核心表，包含主键、外键、索引、注释
--   2. AI相关数据表：attendance_alert（异常预警）、
--      performance_record（绩效分析）、
--      user_behavior_log（用户行为日志）
--   3. 后续适配达梦数据库时，需调整：
--      BIGINT AUTO_INCREMENT → IDENTITY
--      TINYINT → SMALLINT
--      DATETIME → TIMESTAMP
--      VARCHAR → VARCHAR2
--      TEXT → CLOB
-- =====================================================

-- 设置字符编码（必须，否则中文数据插入失败）
SET NAMES utf8mb4;

DROP DATABASE IF EXISTS attendance_system;
CREATE DATABASE attendance_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE attendance_system;

-- =====================================================
-- 1. 部门表 (department)
-- =====================================================
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    manager VARCHAR(50) COMMENT '部门负责人',
    manager_phone VARCHAR(20) COMMENT '负责人电话',
    member_count INT DEFAULT 0 COMMENT '部门人数',
    description VARCHAR(500) COMMENT '部门描述',
    status VARCHAR(20) DEFAULT '正常' COMMENT '状态',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_dept_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- =====================================================
-- 2. 员工表 (employee)
-- =====================================================
CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    employee_id VARCHAR(20) NOT NULL UNIQUE COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    department_id BIGINT COMMENT '所属部门ID',
    department_name VARCHAR(100) COMMENT '部门名称',
    position VARCHAR(100) COMMENT '职位',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    hire_date DATE COMMENT '入职日期',
    status VARCHAR(20) DEFAULT '在职' COMMENT '状态',
    attendance_rate DECIMAL(5,2) DEFAULT 100.00 COMMENT '出勤率',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_employee_dept (department_id),
    INDEX idx_employee_status (status),
    INDEX idx_employee_name (name),
    CONSTRAINT fk_employee_dept FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- =====================================================
-- 3. 系统用户表 (sys_user)
-- =====================================================
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    employee_id VARCHAR(20) COMMENT '关联工号',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(50) NOT NULL COMMENT '角色',
    phone VARCHAR(20) COMMENT '手机号',
    status VARCHAR(20) DEFAULT '正常' COMMENT '状态',
    last_login DATETIME COMMENT '最近登录时间',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- =====================================================
-- 4. 考勤记录表 (attendance_record)
-- =====================================================
CREATE TABLE attendance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    employee_id VARCHAR(20) NOT NULL COMMENT '工号',
    employee_name VARCHAR(100) COMMENT '员工姓名',
    department_name VARCHAR(100) COMMENT '部门名称',
    position VARCHAR(100) COMMENT '职位',
    record_date DATE NOT NULL COMMENT '考勤日期',
    clock_in VARCHAR(10) COMMENT '上班打卡',
    clock_out VARCHAR(10) COMMENT '下班打卡',
    work_hours DECIMAL(4,1) DEFAULT 0 COMMENT '工作时长',
    status VARCHAR(20) DEFAULT '正常' COMMENT '状态',
    is_late TINYINT(1) DEFAULT 0 COMMENT '是否迟到',
    is_early TINYINT(1) DEFAULT 0 COMMENT '是否早退',
    create_time DATETIME COMMENT '创建时间',
    INDEX idx_record_date (record_date),
    INDEX idx_record_emp (employee_id),
    INDEX idx_record_dept (department_name),
    UNIQUE KEY uk_emp_date (employee_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- =====================================================
-- 5. 请假申请表 (leave_request)
-- =====================================================
CREATE TABLE leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '请假ID',
    request_no VARCHAR(30) NOT NULL UNIQUE COMMENT '申请编号',
    employee_id VARCHAR(20) COMMENT '工号',
    employee_name VARCHAR(100) NOT NULL COMMENT '申请人姓名',
    department_name VARCHAR(100) COMMENT '部门',
    leave_type VARCHAR(20) NOT NULL COMMENT '请假类型',
    days DECIMAL(3,1) NOT NULL COMMENT '请假天数',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    reason VARCHAR(500) COMMENT '请假原因',
    approver VARCHAR(50) COMMENT '审批人',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
    approve_time DATETIME COMMENT '审批时间',
    approve_comment VARCHAR(500) COMMENT '审批意见',
    create_time DATETIME COMMENT '申请时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_leave_status (status),
    INDEX idx_leave_emp (employee_id),
    INDEX idx_leave_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- =====================================================
-- 6. AI异常预警表 (attendance_alert)
-- =====================================================
CREATE TABLE attendance_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预警ID',
    alert_no VARCHAR(30) NOT NULL UNIQUE COMMENT '预警编号',
    employee_id VARCHAR(20) NOT NULL COMMENT '工号',
    employee_name VARCHAR(100) COMMENT '员工姓名',
    department_name VARCHAR(100) COMMENT '部门',
    position VARCHAR(100) COMMENT '职位',
    phone VARCHAR(20) COMMENT '手机号',
    level VARCHAR(10) COMMENT '风险等级',
    summary VARCHAR(500) COMMENT '异常概述',
    ai_suggestion TEXT COMMENT 'AI改进建议',
    status VARCHAR(20) DEFAULT '待处理' COMMENT '状态',
    handle_time DATETIME COMMENT '处理时间',
    handler VARCHAR(50) COMMENT '处理人',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_alert_level (level),
    INDEX idx_alert_status (status),
    INDEX idx_alert_emp (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI异常预警表';

-- =====================================================
-- 7. AI绩效分析表 (performance_record)
-- =====================================================
CREATE TABLE performance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '绩效ID',
    employee_id VARCHAR(20) NOT NULL COMMENT '工号',
    employee_name VARCHAR(100) COMMENT '员工姓名',
    department_name VARCHAR(100) COMMENT '部门',
    position VARCHAR(100) COMMENT '职位',
    analysis_period VARCHAR(20) DEFAULT '本月' COMMENT '分析周期',
    attendance_rate DECIMAL(5,2) COMMENT '出勤率',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_count INT DEFAULT 0 COMMENT '早退次数',
    leave_days DECIMAL(3,1) DEFAULT 0 COMMENT '请假天数',
    attendance_score DECIMAL(5,1) COMMENT '考勤得分',
    task_score DECIMAL(5,1) COMMENT '任务得分',
    collaboration_score DECIMAL(5,1) COMMENT '协作得分',
    final_score DECIMAL(5,1) COMMENT '综合得分',
    score_level VARCHAR(20) COMMENT '评级',
    ai_comment TEXT COMMENT 'AI综合评语',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_perf_emp (employee_id),
    INDEX idx_perf_level (score_level),
    INDEX idx_perf_dept (department_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI绩效分析表';

-- =====================================================
-- 8. 考勤统计表 (attendance_summary)
-- =====================================================
CREATE TABLE attendance_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    summary_date DATE NOT NULL UNIQUE COMMENT '统计日期',
    total_employees INT DEFAULT 0 COMMENT '员工总数',
    actual_attendance INT DEFAULT 0 COMMENT '实际出勤人数',
    attendance_rate DECIMAL(5,2) DEFAULT 0 COMMENT '出勤率',
    late_count INT DEFAULT 0 COMMENT '迟到人数',
    early_count INT DEFAULT 0 COMMENT '早退人数',
    leave_count INT DEFAULT 0 COMMENT '请假人数',
    missing_count INT DEFAULT 0 COMMENT '缺卡人数',
    new_hire_count INT DEFAULT 0 COMMENT '本月新入职人数',
    pending_leave_count INT DEFAULT 0 COMMENT '待审批请假数',
    emergency_count INT DEFAULT 0 COMMENT '紧急单据数',
    alert_count INT DEFAULT 0 COMMENT '异常预警数',
    create_time DATETIME COMMENT '创建时间',
    INDEX idx_summary_date (summary_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤统计表';

-- =====================================================
-- 9. 用户行为日志表 (user_behavior_log) - AI数据支撑
-- =====================================================
CREATE TABLE user_behavior_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '账号',
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    action_desc VARCHAR(500) COMMENT '操作描述',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id VARCHAR(50) COMMENT '目标ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    cost_time INT DEFAULT 0 COMMENT '耗时',
    create_time DATETIME COMMENT '操作时间',
    INDEX idx_behavior_user (user_id),
    INDEX idx_behavior_type (action_type),
    INDEX idx_behavior_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- =====================================================
-- 10. AI推荐日志表 (ai_suggestion_log) - AI数据支撑
-- =====================================================
CREATE TABLE ai_suggestion_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    suggestion_type VARCHAR(50) NOT NULL COMMENT '推荐类型',
    input_data TEXT COMMENT '输入数据',
    output_data TEXT COMMENT 'AI输出结果',
    trigger_source VARCHAR(50) COMMENT '触发来源',
    status VARCHAR(20) DEFAULT 'completed' COMMENT '状态',
    cost_time INT DEFAULT 0 COMMENT 'AI处理耗时',
    create_time DATETIME COMMENT '创建时间',
    INDEX idx_ai_type (suggestion_type),
    INDEX idx_ai_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐日志表';

-- =====================================================
-- 11. 时间要求表 (time_requirement)
-- =====================================================
CREATE TABLE time_requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    monday_time VARCHAR(10) DEFAULT '09:00' COMMENT '周一上班打卡时间',
    tuesday_time VARCHAR(10) DEFAULT '09:00' COMMENT '周二上班打卡时间',
    wednesday_time VARCHAR(10) DEFAULT '09:00' COMMENT '周三上班打卡时间',
    thursday_time VARCHAR(10) DEFAULT '09:00' COMMENT '周四上班打卡时间',
    friday_time VARCHAR(10) DEFAULT '09:00' COMMENT '周五上班打卡时间',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_dept_id (dept_id),
    CONSTRAINT fk_time_req_dept FOREIGN KEY (dept_id) REFERENCES department(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间要求表';

-- =====================================================
-- 12. AI配置表 (ai_config)
-- =====================================================
CREATE TABLE ai_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    api_key VARCHAR(500) COMMENT 'AES加密的API Key',
    api_url VARCHAR(300) COMMENT 'API请求地址，留空默认DeepSeek',
    alert_threshold INT DEFAULT 80 COMMENT '预警阈值，绩效综合评分低于此值触发预警',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI配置表';

-- =====================================================
-- 初始数据
-- =====================================================

-- 部门初始数据（经理均为真实员工）
INSERT INTO department (id, name, manager, manager_phone, member_count, description, status, create_time, update_time) VALUES
(1, '技术部', '赵文远', '13800001001', 8, '负责公司核心产品的研发与维护', '正常', NOW(), NOW()),
(2, '市场部', '陈思琪', '13800001002', 6, '负责全国市场开拓与销售推广', '正常', NOW(), NOW()),
(3, '人事部', '林雨桐', '13800001003', 5, '负责招聘、培训、绩效考核等人力资源管理', '正常', NOW(), NOW()),
(4, '财务部', '郑明杰', '13800001004', 6, '负责公司财务审计与报销管理', '正常', NOW(), NOW()),
(5, '研发部', '赵文远', '13800001001', 8, '负责核心系统架构设计与后端开发', '正常', NOW(), NOW()),
(6, '产品部', '李若涵', '13800001005', 5, '负责产品规划与需求设计', '正常', NOW(), NOW()),
(7, '销售部', '陈思琪', '13800001002', 7, '负责全国市场的客户开拓与维护', '正常', NOW(), NOW()),
(8, '行政部', '林雨桐', '13800001003', 5, '负责行政后勤与办公环境管理', '正常', NOW(), NOW());

-- 员工初始数据 (50人)
INSERT INTO employee (employee_id, name, department_id, department_name, position, phone, attendance_rate, status, create_time, update_time) VALUES
-- 技术部 (8人) — 经理赵文远
('EMP001', '赵文远', 1, '技术部', '技术总监', '13800001001', 99.00, '在职', NOW(), NOW()),
('EMP002', '张晓敏', 1, '技术部', '后端开发', '13800010001', 98.00, '在职', NOW(), NOW()),
('EMP003', '李志鹏', 1, '技术部', '前端开发', '13800010002', 95.40, '在职', NOW(), NOW()),
('EMP004', '周八', 1, '技术部', '测试工程师', '13800010006', 85.00, '在职', NOW(), NOW()),
('EMP005', '吴九', 1, '技术部', '后端开发', '13800010007', 96.50, '在职', NOW(), NOW()),
('EMP006', '郑十', 1, '技术部', '前端开发', '13800010008', 93.20, '在职', NOW(), NOW()),
('EMP007', '冯十一', 1, '技术部', '运维工程师', '13800010009', 97.80, '在职', NOW(), NOW()),
('EMP008', '褚十二', 1, '技术部', '测试工程师', '13800010010', 91.00, '试用', NOW(), NOW()),
-- 市场部 (6人) — 经理陈思琪
('EMP009', '陈思琪', 2, '市场部', '市场总监', '13800001002', 97.00, '在职', NOW(), NOW()),
('EMP010', '陈嘉怡', 2, '市场部', '运营专员', '13800010003', 93.60, '在职', NOW(), NOW()),
('EMP011', '卫十三', 2, '市场部', '品牌策划', '13800010011', 94.80, '在职', NOW(), NOW()),
('EMP012', '蒋十四', 2, '市场部', '市场专员', '13800010012', 90.50, '在职', NOW(), NOW()),
('EMP013', '沈十五', 2, '市场部', '新媒体运营', '13800010013', 96.30, '试用', NOW(), NOW()),
('EMP014', '韩十六', 2, '市场部', '渠道拓展', '13800010014', 88.90, '在职', NOW(), NOW()),
-- 人事部 (5人) — 经理林雨桐
('EMP015', '林雨桐', 3, '人事部', '人事总监', '13800001003', 99.50, '在职', NOW(), NOW()),
('EMP016', '黄思颖', 3, '人事部', 'HRBP', '13800010004', 97.30, '试用', NOW(), NOW()),
('EMP017', '杨十八', 3, '人事部', '招聘专员', '13800010017', 95.60, '在职', NOW(), NOW()),
('EMP018', '朱十九', 3, '人事部', '培训专员', '13800010018', 94.20, '在职', NOW(), NOW()),
('EMP019', '秦二十', 3, '人事部', '薪酬专员', '13800010019', 96.80, '在职', NOW(), NOW()),
-- 财务部 (6人) — 经理郑明杰
('EMP020', '郑明杰', 4, '财务部', '财务总监', '13800001004', 99.20, '在职', NOW(), NOW()),
('EMP021', '刘文涛', 4, '财务部', '会计', '13800010005', 99.10, '在职', NOW(), NOW()),
('EMP022', '尤二一', 4, '财务部', '出纳', '13800010021', 97.40, '在职', NOW(), NOW()),
('EMP023', '许二二', 4, '财务部', '审计员', '13800010022', 95.10, '在职', NOW(), NOW()),
('EMP024', '何二三', 4, '财务部', '会计', '13800010023', 92.70, '在职', NOW(), NOW()),
('EMP025', '吕二四', 4, '财务部', '出纳', '13800010024', 93.80, '试用', NOW(), NOW()),
-- 研发部 (8人) — 经理赵文远 (EMP001兼)
('EMP026', '张三', 5, '研发部', '架构师', '13800010026', 96.00, '在职', NOW(), NOW()),
('EMP027', '李四', 5, '研发部', '后端工程师', '13800010027', 92.00, '在职', NOW(), NOW()),
('EMP028', '施二五', 5, '研发部', '前端工程师', '13800010028', 94.50, '在职', NOW(), NOW()),
('EMP029', '张二六', 5, '研发部', '算法工程师', '13800010029', 97.20, '在职', NOW(), NOW()),
('EMP030', '孔二七', 5, '研发部', '后端工程师', '13800010030', 89.60, '在职', NOW(), NOW()),
('EMP031', '曹二八', 5, '研发部', '前端工程师', '13800010031', 95.80, '试用', NOW(), NOW()),
('EMP032', '严二九', 5, '研发部', '测试开发', '13800010032', 91.30, '在职', NOW(), NOW()),
('EMP033', '华三十', 5, '研发部', 'DevOps', '13800010033', 93.90, '在职', NOW(), NOW()),
-- 产品部 (5人) — 经理李若涵
('EMP034', '李若涵', 6, '产品部', '产品总监', '13800001005', 98.50, '在职', NOW(), NOW()),
('EMP035', '金三一', 6, '产品部', '产品经理', '13800010035', 91.20, '在职', NOW(), NOW()),
('EMP036', '魏三二', 6, '产品部', '产品经理', '13800010036', 89.80, '在职', NOW(), NOW()),
('EMP037', '陶三三', 6, '产品部', 'UI设计师', '13800010037', 95.40, '在职', NOW(), NOW()),
('EMP038', '姜三四', 6, '产品部', '交互设计师', '13800010038', 94.10, '试用', NOW(), NOW()),
-- 销售部 (7人) — 经理陈思琪 (EMP009兼)
('EMP039', '王五', 7, '销售部', '销售经理', '13800010039', 88.00, '在职', NOW(), NOW()),
('EMP040', '赵六', 7, '销售部', '销售专员', '13800010040', 99.00, '在职', NOW(), NOW()),
('EMP041', '钱七', 7, '销售部', '销售专员', '13800010041', 97.00, '在职', NOW(), NOW()),
('EMP042', '戚三五', 7, '销售部', '大客户经理', '13800010042', 92.50, '在职', NOW(), NOW()),
('EMP043', '谢三六', 7, '销售部', '销售专员', '13800010043', 86.30, '在职', NOW(), NOW()),
('EMP044', '邹三七', 7, '销售部', '区域经理', '13800010044', 90.70, '在职', NOW(), NOW()),
('EMP045', '苏三八', 7, '销售部', '销售专员', '13800010045', 94.80, '试用', NOW(), NOW()),
-- 行政部 (5人) — 经理林雨桐 (EMP015兼)
('EMP046', '孙七', 8, '行政部', '行政主管', '13800010046', 95.00, '在职', NOW(), NOW()),
('EMP047', '潘三九', 8, '行政部', '行政专员', '13800010047', 93.50, '在职', NOW(), NOW()),
('EMP048', '范四十', 8, '行政部', '前台', '13800010048', 96.20, '在职', NOW(), NOW()),
('EMP049', '彭四一', 8, '行政部', '后勤管理', '13800010049', 91.40, '在职', NOW(), NOW()),
('EMP050', '韦四二', 8, '行政部', '行政专员', '13800010050', 94.60, '试用', NOW(), NOW());

-- 系统用户初始数据（密码明文均为123456，BCrypt加密存储）
INSERT INTO sys_user (employee_id, username, password, role, phone, status, last_login, create_time, update_time) VALUES
('EMP000', 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '系统管理员', '13800000001', '正常', '2026-06-20 08:30', NOW(), NOW()),
('EMP000', 'hr_manager', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '人事主管', '13800000002', '正常', '2026-06-20 09:02', NOW(), NOW()),
('EMP000', 'dept_leader', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '部门主管', '13800000003', '正常', '2026-06-18 18:15', NOW(), NOW());

-- 考勤记录初始数据（部分员工最近3天记录）
INSERT INTO attendance_record (employee_id, employee_name, department_name, position, record_date, clock_in, clock_out, work_hours, status, is_late, is_early, create_time) VALUES
('EMP001', '赵文远', '技术部', '技术总监', '2026-06-20', '08:30', '19:15', 10.0, '正常', 0, 0, NOW()),
('EMP002', '张晓敏', '技术部', '后端开发', '2026-06-20', '08:52', '18:02', 8.5, '正常', 0, 0, NOW()),
('EMP003', '李志鹏', '技术部', '前端开发', '2026-06-20', '09:13', '18:00', 8.1, '迟到', 1, 0, NOW()),
('EMP010', '陈嘉怡', '市场部', '运营专员', '2026-06-20', '08:47', '17:56', 8.0, '正常', 0, 0, NOW()),
('EMP016', '黄思颖', '人事部', 'HRBP', '2026-06-20', '--', '--', 0, '请假', 0, 0, NOW()),
('EMP021', '刘文涛', '财务部', '会计', '2026-06-20', '08:58', '--', 4.2, '缺卡', 0, 0, NOW()),
('EMP002', '张晓敏', '技术部', '后端开发', '2026-06-19', '08:55', '18:02', 8.5, '正常', 0, 0, NOW()),
('EMP003', '李志鹏', '技术部', '前端开发', '2026-06-19', '09:20', '18:00', 8.0, '迟到', 1, 0, NOW()),
('EMP002', '张晓敏', '技术部', '后端开发', '2026-06-18', '08:55', '18:02', 8.5, '正常', 0, 0, NOW()),
('EMP003', '李志鹏', '技术部', '前端开发', '2026-06-18', '09:12', '18:00', 8.0, '迟到', 1, 0, NOW()),
('EMP004', '周八', '技术部', '测试工程师', '2026-06-18', '09:25', '18:00', 7.8, '迟到', 1, 0, NOW()),
('EMP004', '周八', '技术部', '测试工程师', '2026-06-17', '09:20', '18:00', 8.0, '迟到', 1, 0, NOW()),
('EMP040', '赵六', '销售部', '销售专员', '2026-06-20', '08:40', '17:55', 8.5, '正常', 0, 0, NOW()),
('EMP039', '王五', '销售部', '销售经理', '2026-06-19', '09:35', '17:30', 7.2, '迟到', 1, 0, NOW()),
('EMP026', '张三', '研发部', '架构师', '2026-06-20', '08:55', '18:10', 8.5, '正常', 0, 0, NOW());

-- 请假申请初始数据
INSERT INTO leave_request (request_no, employee_name, employee_id, department_name, leave_type, days, start_date, end_date, reason, approver, status, create_time, update_time) VALUES
('LV20260620001', '黄思颖', 'EMP016', '人事部', '病假', 1, '2026-06-20', '2026-06-20', '发烧就医', '林雨桐', 'pending', NOW(), NOW()),
('LV20260619001', '陈嘉怡', 'EMP010', '市场部', '事假', 2, '2026-06-24', '2026-06-25', '家庭事务', '陈思琪', 'pending', NOW(), NOW()),
('LV20260618001', '李志鹏', 'EMP003', '技术部', '调休', 0.5, '2026-06-22', '2026-06-22', '加班调休', '赵文远', 'approved', NOW(), NOW()),
('LV20260617001', '张晓敏', 'EMP002', '技术部', '年假', 3, '2026-06-27', '2026-06-29', '回老家探亲', '赵文远', 'approved', NOW(), NOW()),
('LV20260616001', '赵六', 'EMP040', '销售部', '事假', 1, '2026-06-23', '2026-06-23', '办理证件', '陈思琪', 'pending', NOW(), NOW()),
('LV20260615001', '周八', 'EMP004', '技术部', '病假', 2, '2026-06-26', '2026-06-27', '急性肠胃炎', '赵文远', 'pending', NOW(), NOW()),
('LV20260614001', '李四', 'EMP027', '研发部', '婚假', 5, '2026-07-01', '2026-07-05', '结婚', '赵文远', 'approved', NOW(), NOW()),
('LV20260613001', '王五', 'EMP039', '销售部', '调休', 1, '2026-06-28', '2026-06-28', '周末加班调休', '陈思琪', 'approved', NOW(), NOW()),
('LV20260612001', '张三', 'EMP026', '研发部', '事假', 0.5, '2026-06-25', '2026-06-25', '孩子家长会', '赵文远', 'pending', NOW(), NOW()),
('LV20260611001', '孙七', 'EMP046', '行政部', '年假', 2, '2026-06-30', '2026-07-01', '短途旅行', '林雨桐', 'approved', NOW(), NOW()),
('LV20260610001', '陈嘉怡', 'EMP010', '市场部', '调休', 1, '2026-07-03', '2026-07-03', '法定假日值班调休', '陈思琪', 'pending', NOW(), NOW()),
('LV20260609001', '黄思颖', 'EMP016', '人事部', '事假', 0.5, '2026-06-21', '2026-06-21', '体检', '林雨桐', 'approved', NOW(), NOW());

-- AI异常预警初始数据
INSERT INTO attendance_alert (alert_no, employee_id, employee_name, department_name, position, phone, level, summary, ai_suggestion, status, create_time, update_time) VALUES
('ALT-001', 'EMP003', '李志鹏', '技术部', '前端开发', '13800010002', '高', '', '', '待处理', NOW(), NOW()),
('ALT-002', 'EMP010', '陈嘉怡', '市场部', '运营专员', '13800010003', '中', '', '', '待处理', NOW(), NOW()),
('ALT-003', 'EMP021', '刘文涛', '财务部', '会计', '13800010005', '低', '', '', '已关闭', NOW(), NOW()),
('ALT-004', 'EMP004', '周八', '技术部', '测试工程师', '13800010006', '高', '', '', '待处理', NOW(), NOW()),
('ALT-005', 'EMP027', '李四', '研发部', '后端工程师', '13800010027', '中', '', '', '待处理', NOW(), NOW()),
('ALT-006', 'EMP039', '王五', '销售部', '销售经理', '13800010039', '低', '', '', '待处理', NOW(), NOW()),
('ALT-007', 'EMP040', '赵六', '销售部', '销售专员', '13800010040', '高', '', '', '待处理', NOW(), NOW());

-- AI绩效分析初始数据（50人，ai_comment由AI生成）
INSERT INTO performance_record (employee_id, employee_name, department_name, position, analysis_period, attendance_rate, late_count, early_count, leave_days, attendance_score, task_score, collaboration_score, final_score, score_level, ai_comment, create_time, update_time) VALUES
('EMP001', '赵文远', '技术部', '技术总监', '本月', 99.00, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP002', '张晓敏', '技术部', '后端开发', '本月', 98.00, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP003', '李志鹏', '技术部', '前端开发', '本月', 95.40, 3, 0, 0.5, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP004', '周八', '技术部', '测试工程师', '本月', 85.00, 5, 0, 2, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP005', '吴九', '技术部', '后端开发', '本月', 96.50, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP006', '郑十', '技术部', '前端开发', '本月', 93.20, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP007', '冯十一', '技术部', '运维工程师', '本月', 97.80, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP008', '褚十二', '技术部', '测试工程师', '本月', 91.00, 2, 1, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP009', '陈思琪', '市场部', '市场总监', '本月', 97.00, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP010', '陈嘉怡', '市场部', '运营专员', '本月', 93.60, 1, 0, 2, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP011', '卫十三', '市场部', '品牌策划', '本月', 94.80, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP012', '蒋十四', '市场部', '市场专员', '本月', 90.50, 2, 1, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP013', '沈十五', '市场部', '新媒体运营', '本月', 96.30, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP014', '韩十六', '市场部', '渠道拓展', '本月', 88.90, 3, 0, 2, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP015', '林雨桐', '人事部', '人事总监', '本月', 99.50, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP016', '黄思颖', '人事部', 'HRBP', '本月', 97.30, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP017', '杨十八', '人事部', '招聘专员', '本月', 95.60, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP018', '朱十九', '人事部', '培训专员', '本月', 94.20, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP019', '秦二十', '人事部', '薪酬专员', '本月', 96.80, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP020', '郑明杰', '财务部', '财务总监', '本月', 99.20, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP021', '刘文涛', '财务部', '会计', '本月', 99.10, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP022', '尤二一', '财务部', '出纳', '本月', 97.40, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP023', '许二二', '财务部', '审计员', '本月', 95.10, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP024', '何二三', '财务部', '会计', '本月', 92.70, 1, 2, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP025', '吕二四', '财务部', '出纳', '本月', 93.80, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP026', '张三', '研发部', '架构师', '本月', 96.00, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP027', '李四', '研发部', '后端工程师', '本月', 92.00, 2, 1, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP028', '施二五', '研发部', '前端工程师', '本月', 94.50, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP029', '张二六', '研发部', '算法工程师', '本月', 97.20, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP030', '孔二七', '研发部', '后端工程师', '本月', 89.60, 3, 1, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP031', '曹二八', '研发部', '前端工程师', '本月', 95.80, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP032', '严二九', '研发部', '测试开发', '本月', 91.30, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP033', '华三十', '研发部', 'DevOps', '本月', 93.90, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP034', '李若涵', '产品部', '产品总监', '本月', 98.50, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP035', '金三一', '产品部', '产品经理', '本月', 91.20, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP036', '魏三二', '产品部', '产品经理', '本月', 89.80, 3, 1, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP037', '陶三三', '产品部', 'UI设计师', '本月', 95.40, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP038', '姜三四', '产品部', '交互设计师', '本月', 94.10, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP039', '王五', '销售部', '销售经理', '本月', 88.00, 3, 0, 3, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP040', '赵六', '销售部', '销售专员', '本月', 99.00, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP041', '钱七', '销售部', '销售专员', '本月', 97.00, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP042', '戚三五', '销售部', '大客户经理', '本月', 92.50, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP043', '谢三六', '销售部', '销售专员', '本月', 86.30, 4, 0, 2, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP044', '邹三七', '销售部', '区域经理', '本月', 90.70, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP045', '苏三八', '销售部', '销售专员', '本月', 94.80, 0, 1, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP046', '孙七', '行政部', '行政主管', '本月', 95.00, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP047', '潘三九', '行政部', '行政专员', '本月', 93.50, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP048', '范四十', '行政部', '前台', '本月', 96.20, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP049', '彭四一', '行政部', '后勤管理', '本月', 91.40, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW()),
('EMP050', '韦四二', '行政部', '行政专员', '本月', 94.60, 0, 1, 0, NULL, NULL, NULL, NULL, NULL, '', NOW(), NOW());

-- 考勤统计初始数据
INSERT INTO attendance_summary (summary_date, total_employees, actual_attendance, attendance_rate, late_count, early_count, leave_count, missing_count, new_hire_count, pending_leave_count, emergency_count, alert_count, create_time) VALUES
('2026-06-20', 50, 48, 96.00, 6, 2, 7, 3, 4, 9, 3, 6, NOW()),
('2026-06-19', 50, 48, 96.00, 5, 3, 6, 2, 2, 7, 2, 5, NOW()),
('2026-06-18', 48, 46, 95.83, 4, 1, 8, 2, 3, 5, 1, 4, NOW()),
('2026-06-17', 48, 46, 95.83, 3, 2, 5, 1, 0, 6, 2, 3, NOW()),
('2026-06-16', 48, 47, 97.92, 2, 1, 4, 1, 1, 4, 0, 2, NOW()),
('2026-06-15', 47, 46, 97.87, 3, 0, 6, 1, 0, 5, 1, 2, NOW()),
('2026-06-14', 46, 44, 95.65, 7, 2, 5, 3, 2, 8, 2, 3, NOW());

-- 时间要求初始数据
INSERT INTO time_requirement (dept_id, dept_name, monday_time, tuesday_time, wednesday_time, thursday_time, friday_time, create_time, update_time) VALUES
(1, '技术部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW()),
(2, '市场部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW()),
(3, '人事部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW()),
(4, '财务部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW()),
(5, '研发部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW()),
(6, '产品部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW()),
(7, '销售部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW()),
(8, '行政部', '09:00', '09:00', '09:00', '09:00', '09:00', NOW(), NOW());
