package com.attendance.repository;

import com.attendance.domain.AiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiConfigRepository extends JpaRepository<AiConfig, Long> {
    Optional<AiConfig> findTopByOrderByIdDesc();
}
