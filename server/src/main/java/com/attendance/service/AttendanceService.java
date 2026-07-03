package com.attendance.service;

import com.attendance.common.PageResult;
import com.attendance.domain.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    PageResult<AttendanceRecord> listRecords(int page, int pageSize, String departmentName,
                                              LocalDate startDate, LocalDate endDate);

    AttendanceRecord getById(Long id);

    int judgeByTimeRequirements();
}
