package com.attendance.service.impl;

import com.attendance.common.PageResult;
import com.attendance.domain.AttendanceRecord;
import com.attendance.domain.TimeRequirement;
import com.attendance.repository.AttendanceRecordRepository;
import com.attendance.repository.TimeRequirementRepository;
import com.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository recordRepository;
    private final TimeRequirementRepository timeRequirementRepository;

    @Override
    public PageResult<AttendanceRecord> listRecords(int page, int pageSize, String departmentName,
                                                     LocalDate startDate, LocalDate endDate) {
        Page<AttendanceRecord> result = recordRepository.findByFilters(
                null, departmentName, startDate, endDate,
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "recordDate")));
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, pageSize);
    }

    @Override
    public AttendanceRecord getById(Long id) {
        return recordRepository.findById(id).orElse(null);
    }

    /**
     * 基于各部门时间要求，重新判定所有考勤记录的打卡状态
     * @return 更新的记录数量
     */
    @Override
    @Transactional
    public int judgeByTimeRequirements() {
        // 1. 加载所有时间要求，按部门名称建索引
        List<TimeRequirement> trList = timeRequirementRepository.findAll();
        Map<String, TimeRequirement> trMap = trList.stream()
                .collect(Collectors.toMap(
                        TimeRequirement::getDeptName,
                        tr -> tr,
                        (a, b) -> a
                ));
        log.info("加载时间要求: {} 个部门", trMap.size());

        // 2. 遍历所有考勤记录，逐条判定
        List<AttendanceRecord> allRecords = recordRepository.findAll();
        int updatedCount = 0;

        for (AttendanceRecord record : allRecords) {
            TimeRequirement tr = trMap.get(record.getDepartmentName());
            if (tr == null) {
                // 该部门没有设置时间要求，跳过
                continue;
            }

            String clockIn = record.getClockIn();
            if (clockIn == null || clockIn.equals("--") || clockIn.isEmpty()) {
                // 未打卡，保持现有状态
                continue;
            }

            // 根据日期获取对应的上班时间门槛
            String threshold = getThresholdByDayOfWeek(tr, record.getRecordDate());
            if (threshold == null) {
                continue;
            }

            // 比较打卡时间与门槛
            boolean isLate = clockIn.compareTo(threshold) > 0;

            // 更新迟到/正常状态
            int oldLate = record.getIsLate() != null ? record.getIsLate() : 0;
            int newLate = isLate ? 1 : 0;

            if (oldLate != newLate || !"正常".equals(record.getStatus()) && !isLate) {
                record.setIsLate(newLate);
                if (isLate) {
                    record.setStatus("迟到");
                } else {
                    // 非迟到且之前是迟到状态，且不是缺卡/请假等
                    if ("迟到".equals(record.getStatus())) {
                        record.setStatus("正常");
                    }
                }
                recordRepository.save(record);
                updatedCount++;
            }
        }

        log.info("考勤判定完成，更新 {} 条记录", updatedCount);
        return updatedCount;
    }

    /**
     * 根据星期几获取该部门的上班打卡时间门槛
     */
    private String getThresholdByDayOfWeek(TimeRequirement tr, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        switch (dow) {
            case MONDAY:    return tr.getMondayTime();
            case TUESDAY:   return tr.getTuesdayTime();
            case WEDNESDAY: return tr.getWednesdayTime();
            case THURSDAY:  return tr.getThursdayTime();
            case FRIDAY:    return tr.getFridayTime();
            default:        return null; // 周末不判定
        }
    }
}
