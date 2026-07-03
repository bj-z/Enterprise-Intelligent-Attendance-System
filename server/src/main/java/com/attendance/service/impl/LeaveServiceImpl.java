package com.attendance.service.impl;

import com.attendance.common.BusinessException;
import com.attendance.common.PageResult;
import com.attendance.domain.LeaveRequest;
import com.attendance.repository.LeaveRequestRepository;
import com.attendance.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public PageResult<LeaveRequest> listByTab(int page, int pageSize, String tab) {
        String status = "processed".equals(tab) ? null : "pending";
        Page<LeaveRequest> result;
        if (status == null) {
            // 已处理：不是pending的
            result = leaveRequestRepository.findAll(
                    (root, query, cb) -> cb.notEqual(root.get("status"), "pending"),
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
        } else {
            result = leaveRequestRepository.findByStatus(status,
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime")));
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, pageSize);
    }

    @Override
    @Transactional
    public LeaveRequest approve(Long id, String approver) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("请假申请不存在"));
        if (!"pending".equals(request.getStatus())) {
            throw new BusinessException("该申请已处理");
        }
        request.setStatus("approved");
        request.setApprover(approver);
        request.setApproveTime(LocalDateTime.now());
        return leaveRequestRepository.save(request);
    }

    @Override
    @Transactional
    public LeaveRequest reject(Long id, String approver, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("请假申请不存在"));
        if (!"pending".equals(request.getStatus())) {
            throw new BusinessException("该申请已处理");
        }
        request.setStatus("rejected");
        request.setApprover(approver);
        request.setApproveTime(LocalDateTime.now());
        request.setApproveComment(comment);
        return leaveRequestRepository.save(request);
    }
}
