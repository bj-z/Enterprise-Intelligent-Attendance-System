package com.attendance.service.impl;

import com.attendance.common.BusinessException;
import com.attendance.common.PageResult;
import com.attendance.domain.Employee;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import com.attendance.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public PageResult<Employee> listByPage(int page, int pageSize, String keyword, String department, String status) {
        Page<Employee> result = employeeRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String kw = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), kw),
                        cb.like(cb.lower(root.get("employeeId")), kw.toLowerCase())
                ));
            }
            if (department != null && !department.isEmpty()) {
                predicates.add(cb.equal(root.get("departmentName"), department));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id")));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, pageSize);
    }

    @Override
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
    }

    @Override
    @Transactional
    public Employee create(Employee employee) {
        // 自动生成工号
        String maxEmployeeId = employeeRepository.findAll().stream()
                .map(Employee::getEmployeeId)
                .filter(eid -> eid != null && eid.startsWith("EMP"))
                .max(String::compareTo)
                .orElse("EMP000");
        int nextNum = Integer.parseInt(maxEmployeeId.replace("EMP", "")) + 1;
        employee.setEmployeeId(String.format("EMP%03d", nextNum));
        employee.setStatus("在职");
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee update(Long id, Employee employee) {
        Employee exist = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        exist.setName(employee.getName());
        exist.setDepartmentId(employee.getDepartmentId());
        exist.setDepartmentName(employee.getDepartmentName());
        exist.setPosition(employee.getPosition());
        exist.setPhone(employee.getPhone());
        exist.setEmail(employee.getEmail());
        exist.setStatus(employee.getStatus());
        return employeeRepository.save(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        employeeRepository.delete(employee);
    }
}
