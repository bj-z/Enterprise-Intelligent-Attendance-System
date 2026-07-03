package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.domain.Department;
import com.attendance.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "获取部门列表")
    @GetMapping
    public Result<List<Department>> list() {
        return Result.success(departmentService.listAll());
    }

    @Operation(summary = "获取部门详情")
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public Result<Department> create(@RequestBody Department department) {
        return Result.success(departmentService.create(department));
    }

    @Operation(summary = "编辑部门")
    @PutMapping("/{id}")
    public Result<Department> update(@PathVariable Long id, @RequestBody Department department) {
        return Result.success(departmentService.update(id, department));
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}
