package com.attendance.service;

import com.attendance.common.PageResult;
import com.attendance.domain.Employee;

public interface EmployeeService {

    PageResult<Employee> listByPage(int page, int pageSize, String keyword, String department, String status);

    Employee getById(Long id);

    Employee create(Employee employee);

    Employee update(Long id, Employee employee);

    void delete(Long id);
}
