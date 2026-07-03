package com.attendance.repository;

import com.attendance.domain.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, Long> {

    Optional<AttendanceSummary> findBySummaryDate(LocalDate summaryDate);

    List<AttendanceSummary> findBySummaryDateBetweenOrderBySummaryDate(LocalDate startDate, LocalDate endDate);
}
