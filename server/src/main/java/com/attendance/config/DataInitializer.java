package com.attendance.config;

import com.attendance.repository.*;
import com.attendance.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据初始化器
 * 如果数据库为空，自动插入初始数据（SQL文件已包含，此作为备用）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository sysUserRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceAlertRepository attendanceAlertRepository;
    private final PerformanceRecordRepository performanceRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (sysUserRepository.count() == 0) {
            log.info("检测到空数据库，开始初始化基础数据...");
            initData();
            log.info("基础数据初始化完成");
        } else {
            log.info("数据库已有数据，修复管理员密码...");
            // 每次启动确保默认账户密码正确
            String encodedPwd = passwordEncoder.encode("123456");
            sysUserRepository.findByUsername("admin").ifPresent(u -> {
                u.setPassword(encodedPwd);
                sysUserRepository.save(u);
            });
            sysUserRepository.findByUsername("hr_manager").ifPresent(u -> {
                u.setPassword(encodedPwd);
                sysUserRepository.save(u);
            });
            sysUserRepository.findByUsername("dept_leader").ifPresent(u -> {
                u.setPassword(encodedPwd);
                sysUserRepository.save(u);
            });
            log.info("密码修复完成");
        }
    }

    private void initData() {
        // 初始化部门
        Department tech = createDept("技术部", "赵文远", "13800001001", "负责公司核心产品的研发与维护");
        Department market = createDept("市场部", "陈思琪", "13800001002", "负责全国市场开拓与销售推广");
        Department hr = createDept("人事部", "林雨桐", "13800001003", "负责招聘、培训、绩效考核等人力资源管理");
        Department finance = createDept("财务部", "郑明杰", "13800001004", "负责公司财务审计与报销管理");
        List<Department> depts = departmentRepository.saveAll(List.of(tech, market, hr, finance));

        // 初始化系统用户
        String encodedPwd = passwordEncoder.encode("123456");
        sysUserRepository.saveAll(List.of(
                createUser("EMP000", "admin", encodedPwd, "系统管理员", "13800000001"),
                createUser("EMP000", "hr_manager", encodedPwd, "人事主管", "13800000002"),
                createUser("EMP000", "dept_leader", encodedPwd, "部门主管", "13800000003")
        ));
    }

    private Department createDept(String name, String manager, String managerPhone, String desc) {
        Department d = new Department();
        d.setName(name);
        d.setManager(manager);
        d.setManagerPhone(managerPhone);
        d.setDescription(desc);
        d.setStatus("正常");
        d.setCreateTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        return d;
    }

    private SysUser createUser(String empId, String username, String password, String role, String phone) {
        SysUser u = new SysUser();
        u.setEmployeeId(empId);
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        u.setPhone(phone);
        u.setStatus("正常");
        u.setCreateTime(LocalDateTime.now());
        u.setUpdateTime(LocalDateTime.now());
        return u;
    }
}
