package com.attendance.service;

import com.attendance.common.PageResult;
import com.attendance.domain.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> listAll();

    PageResult<Department> listByPage(int page, int pageSize);

    Department getById(Long id);

    Department create(Department department);

    Department update(Long id, Department department);

    void delete(Long id);
}
