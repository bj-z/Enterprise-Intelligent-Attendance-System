package com.attendance.controller;

import com.attendance.common.PageResult;
import com.attendance.common.Result;
import com.attendance.domain.LeaveRequest;
import com.attendance.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "请假审批")
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @Operation(summary = "分页查询请假列表（tab: pending/processed）")
    @GetMapping("/list")
    public Result<PageResult<LeaveRequest>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "pending") String tab) {
        return Result.success(leaveService.listByTab(page, pageSize, tab));
    }

    @Operation(summary = "同意请假")
    @PutMapping("/{id}/approve")
    public Result<LeaveRequest> approve(@PathVariable Long id,
                                         @RequestParam(defaultValue = "系统管理员") String approver) {
        return Result.success(leaveService.approve(id, approver));
    }

    @Operation(summary = "拒绝请假")
    @PutMapping("/{id}/reject")
    public Result<LeaveRequest> reject(@PathVariable Long id,
                                        @RequestParam(defaultValue = "系统管理员") String approver,
                                        @RequestParam(defaultValue = "审核不通过") String comment) {
        return Result.success(leaveService.reject(id, approver, comment));
    }
}
