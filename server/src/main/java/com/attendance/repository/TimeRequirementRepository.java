package com.attendance.repository;

import com.attendance.domain.TimeRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeRequirementRepository extends JpaRepository<TimeRequirement, Long> {

    Optional<TimeRequirement> findByDeptId(Long deptId);

    boolean existsByDeptId(Long deptId);
}
