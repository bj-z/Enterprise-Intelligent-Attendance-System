package com.attendance.service.impl;

import com.attendance.common.PageResult;
import com.attendance.domain.PerformanceRecord;
import com.attendance.repository.PerformanceRecordRepository;
import com.attendance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRecordRepository performanceRepository;

    @Override
    public PageResult<PerformanceRecord> listByPage(int page, int pageSize, String departmentName) {
        Page<PerformanceRecord> result;
        if (departmentName != null && !departmentName.isEmpty()) {
            result = performanceRepository.findByDepartmentName(departmentName,
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "employeeId")));
        } else {
            result = performanceRepository.findAll(
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "employeeId")));
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, pageSize);
    }

    @Override
    public List<PerformanceRecord> findByIds(List<Long> ids) {
        return performanceRepository.findByIdIn(ids);
    }

    @Override
    public PerformanceRecord save(PerformanceRecord record) {
        return performanceRepository.save(record);
    }

    @Override
    public void saveAll(List<PerformanceRecord> records) {
        performanceRepository.saveAll(records);
    }
}
