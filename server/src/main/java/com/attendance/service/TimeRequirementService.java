package com.attendance.service;

import com.attendance.domain.TimeRequirement;
import java.util.List;

public interface TimeRequirementService {

    List<TimeRequirement> listAll();

    TimeRequirement getByDeptId(Long deptId);

    TimeRequirement saveOrUpdate(Long deptId, TimeRequirement timeRequirement);

    void applyGlobalToAll(TimeRequirement globalTimes);

    void initDefaults();
}
