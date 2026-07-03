package com.attendance.service;

import com.attendance.common.PageResult;
import com.attendance.domain.LeaveRequest;

public interface LeaveService {

    PageResult<LeaveRequest> listByTab(int page, int pageSize, String tab);

    LeaveRequest approve(Long id, String approver);

    LeaveRequest reject(Long id, String approver, String comment);
}
