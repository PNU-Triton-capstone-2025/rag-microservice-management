package com.triton.msa.triton_dashboard.monitoring.service;

import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.entity.MonitoringHistory;
import com.triton.msa.triton_dashboard.monitoring.repository.MonitoringHistoryRepository;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MonitoringHistoryServiceTest {

    @InjectMocks
    private MonitoringHistoryService monitoringHistoryService;

    @Mock
    private MonitoringHistoryRepository monitoringHistoryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("분석 이력 저장 테스트")
    void saveHistory() {
        // given
        Long projectId = 1L;
        RagLogResponseDto analysisDto = new RagLogResponseDto("Error Analysis", "Critical error found.");
        Project mockProject = new Project("test");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        // when
        monitoringHistoryService.saveHistory(projectId, analysisDto);

        // then
        verify(projectRepository).findById(projectId);
        verify(monitoringHistoryRepository).save(any(MonitoringHistory.class));
    }

    @Test
    @DisplayName("분석 이력 저장 실패 - 유효하지 않은 프로젝트 ID")
    void saveHistoryFail() {
        // Given
        Long invalidProjectId = 999L;
        RagLogResponseDto analysisDto = new RagLogResponseDto("Error Analysis", "Critical error found.");

        when(projectRepository.findById(invalidProjectId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            monitoringHistoryService.saveHistory(invalidProjectId, analysisDto);
        });

        verify(projectRepository).findById(invalidProjectId);
    }

    @Test
    @DisplayName("분석 이력 삭제")
    void deleteHistory() {
        // Given
        Long historyId = 1L;

        // When
        monitoringHistoryService.deleteHistory(historyId);

        // Then
        verify(monitoringHistoryRepository).deleteById(historyId);
    }
}
