package com.attendance.service.impl;

import com.attendance.common.BusinessException;
import com.attendance.domain.Department;
import com.attendance.domain.TimeRequirement;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.TimeRequirementRepository;
import com.attendance.service.TimeRequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeRequirementServiceImpl implements TimeRequirementService {

    private final TimeRequirementRepository timeRequirementRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<TimeRequirement> listAll() {
        List<TimeRequirement> list = timeRequirementRepository.findAll(Sort.by(Sort.Direction.ASC, "deptId"));
        // 如果数据库里没有记录，自动初始化
        if (list.isEmpty()) {
            initDefaults();
            list = timeRequirementRepository.findAll(Sort.by(Sort.Direction.ASC, "deptId"));
        }
        return list;
    }

    @Override
    public TimeRequirement getByDeptId(Long deptId) {
        return timeRequirementRepository.findByDeptId(deptId)
                .orElseThrow(() -> new BusinessException("该部门尚未设置时间要求"));
    }

    @Override
    @Transactional
    public TimeRequirement saveOrUpdate(Long deptId, TimeRequirement timeReq) {
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new BusinessException("部门不存在"));

        TimeRequirement exist = timeRequirementRepository.findByDeptId(deptId)
                .orElse(null);

        if (exist == null) {
            exist = new TimeRequirement();
            exist.setDeptId(deptId);
            exist.setDeptName(dept.getName());
        }
        exist.setMondayTime(timeReq.getMondayTime());
        exist.setTuesdayTime(timeReq.getTuesdayTime());
        exist.setWednesdayTime(timeReq.getWednesdayTime());
        exist.setThursdayTime(timeReq.getThursdayTime());
        exist.setFridayTime(timeReq.getFridayTime());

        return timeRequirementRepository.save(exist);
    }

    @Override
    @Transactional
    public void applyGlobalToAll(TimeRequirement globalTimes) {
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            TimeRequirement exist = timeRequirementRepository.findByDeptId(dept.getId())
                    .orElse(null);
            if (exist == null) {
                exist = new TimeRequirement();
                exist.setDeptId(dept.getId());
                exist.setDeptName(dept.getName());
            }
            exist.setMondayTime(globalTimes.getMondayTime());
            exist.setTuesdayTime(globalTimes.getTuesdayTime());
            exist.setWednesdayTime(globalTimes.getWednesdayTime());
            exist.setThursdayTime(globalTimes.getThursdayTime());
            exist.setFridayTime(globalTimes.getFridayTime());
            timeRequirementRepository.save(exist);
        }
    }

    @Override
    @Transactional
    public void initDefaults() {
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            if (!timeRequirementRepository.existsByDeptId(dept.getId())) {
                TimeRequirement tr = new TimeRequirement();
                tr.setDeptId(dept.getId());
                tr.setDeptName(dept.getName());
                timeRequirementRepository.save(tr);
            }
        }
    }
}
