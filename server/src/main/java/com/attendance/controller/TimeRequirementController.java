package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.domain.TimeRequirement;
import com.attendance.service.TimeRequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "时间要求管理")
@RestController
@RequestMapping("/api/time-requirements")
@RequiredArgsConstructor
public class TimeRequirementController {

    private final TimeRequirementService timeRequirementService;

    @Operation(summary = "获取所有部门的时间要求")
    @GetMapping
    public Result<List<TimeRequirement>> listAll() {
        return Result.success(timeRequirementService.listAll());
    }

    @Operation(summary = "获取某部门的时间要求")
    @GetMapping("/{deptId}")
    public Result<TimeRequirement> getByDeptId(@PathVariable Long deptId) {
        return Result.success(timeRequirementService.getByDeptId(deptId));
    }

    @Operation(summary = "保存或更新某部门的时间要求")
    @PutMapping("/{deptId}")
    public Result<TimeRequirement> saveOrUpdate(@PathVariable Long deptId, @RequestBody TimeRequirement timeRequirement) {
        return Result.success(timeRequirementService.saveOrUpdate(deptId, timeRequirement));
    }

    @Operation(summary = "将总体设置应用到所有部门")
    @PostMapping("/apply-global")
    public Result<Void> applyGlobalToAll(@RequestBody TimeRequirement globalTimes) {
        timeRequirementService.applyGlobalToAll(globalTimes);
        return Result.success();
    }

    @Operation(summary = "初始化所有部门的时间要求")
    @PostMapping("/init")
    public Result<Void> initDefaults() {
        timeRequirementService.initDefaults();
        return Result.success();
    }
}
