package com.attendance.service.impl;

import com.attendance.common.PageResult;
import com.attendance.domain.Department;
import com.attendance.domain.Employee;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import com.attendance.service.DepartmentService;
import com.attendance.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<Department> listAll() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public PageResult<Department> listByPage(int page, int pageSize) {
        Page<Department> result = departmentRepository.findAll(
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "id")));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, pageSize);
    }

    @Override
    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));
    }

    @Override
    @Transactional
    public Department create(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new BusinessException("部门名称已存在");
        }
        department.setMemberCount(0);
        department.setStatus("正常");
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department update(Long id, Department department) {
        Department exist = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));
        exist.setName(department.getName());
        exist.setManager(department.getManager());
        exist.setDescription(department.getDescription());
        return departmentRepository.save(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));
        // 检查部门下是否有员工
        long employeeCount = employeeRepository.findAll().stream()
                .filter(e -> e.getDepartmentId() != null && e.getDepartmentId().equals(id))
                .count();
        if (employeeCount > 0) {
            throw new BusinessException("当前部门还有员工，无法删除");
        }
        departmentRepository.delete(department);
    }
}
