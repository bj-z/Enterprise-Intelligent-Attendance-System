package com.attendance.repository;

import com.attendance.domain.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    Page<LeaveRequest> findByStatus(String status, Pageable pageable);

    List<LeaveRequest> findByStatus(String status);

    long countByStatus(String status);
}
