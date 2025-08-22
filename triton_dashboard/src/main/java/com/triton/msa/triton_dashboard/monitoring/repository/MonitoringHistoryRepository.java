package com.triton.msa.triton_dashboard.monitoring.repository;

import com.triton.msa.triton_dashboard.monitoring.entity.MonitoringHistory;
import com.triton.msa.triton_dashboard.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoringHistoryRepository extends JpaRepository<MonitoringHistory, Long> {
    List<MonitoringHistory> findByProjectOrderByCreatedAtDesc(Project project);
    void deleteById(Long historyId);
}
