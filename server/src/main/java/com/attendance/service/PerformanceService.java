package com.attendance.service;

import com.attendance.common.PageResult;
import com.attendance.domain.PerformanceRecord;

import java.util.List;

public interface PerformanceService {

    PageResult<PerformanceRecord> listByPage(int page, int pageSize, String departmentName);

    List<PerformanceRecord> findByIds(List<Long> ids);

    PerformanceRecord save(PerformanceRecord record);

    void saveAll(List<PerformanceRecord> records);
}
