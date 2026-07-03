package com.attendance.controller;

import com.attendance.common.PageResult;
import com.attendance.common.Result;
import com.attendance.domain.Employee;
import com.attendance.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "员工管理")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "分页查询员工列表")
    @GetMapping
    public Result<PageResult<Employee>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        return Result.success(employeeService.listByPage(page, pageSize, keyword, department, status));
    }

    @Operation(summary = "获取员工详情")
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @Operation(summary = "新增员工")
    @PostMapping
    public Result<Employee> create(@RequestBody Employee employee) {
        return Result.success(employeeService.create(employee));
    }

    @Operation(summary = "编辑员工")
    @PutMapping("/{id}")
    public Result<Employee> update(@PathVariable Long id, @RequestBody Employee employee) {
        return Result.success(employeeService.update(id, employee));
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.success();
    }
}
