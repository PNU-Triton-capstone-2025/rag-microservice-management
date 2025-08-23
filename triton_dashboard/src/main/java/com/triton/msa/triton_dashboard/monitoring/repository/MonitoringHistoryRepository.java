package com.triton.msa.triton_dashboard.monitoring.repository;

import com.triton.msa.triton_dashboard.monitoring.entity.MonitoringHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoringHistoryRepository extends JpaRepository<MonitoringHistory, Long> {
    Page<MonitoringHistory> findByProjectId(Long projectId, Pageable pageable);
}
