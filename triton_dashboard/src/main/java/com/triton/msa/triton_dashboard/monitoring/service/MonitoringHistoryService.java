package com.triton.msa.triton_dashboard.monitoring.service;

import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.entity.MonitoringHistory;
import com.triton.msa.triton_dashboard.monitoring.repository.MonitoringHistoryRepository;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MonitoringHistoryService {
    private final MonitoringHistoryRepository monitoringHistoryRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public void saveHistory(Long projectId, RagLogResponseDto analysisDto) {
         Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        MonitoringHistory monitoringHistory = new MonitoringHistory(
                project,
                analysisDto.title(),
                analysisDto.answer(),
                LocalDateTime.now()
        );
        monitoringHistoryRepository.save(monitoringHistory);
    }

    @Transactional
    public void deleteHistory(Long monitoringHistoryId) {
        monitoringHistoryRepository.deleteById(monitoringHistoryId);
    }

    @Transactional(readOnly = true)
    public MonitoringHistory getHistory(Long historyId) {
        return monitoringHistoryRepository.findById(historyId)
                .orElseThrow(() -> new EntityNotFoundException("Monitoring history not found with id: " + historyId));
    }

    @Transactional(readOnly = true)
    public Page<MonitoringHistory> getMonitoringHistories(Long projectId, Pageable pageable) {
        return monitoringHistoryRepository.findByProjectId(projectId, pageable);
    }
}
