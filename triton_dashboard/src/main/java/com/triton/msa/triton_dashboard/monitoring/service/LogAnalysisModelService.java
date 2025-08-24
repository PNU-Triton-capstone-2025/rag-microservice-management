package com.triton.msa.triton_dashboard.monitoring.service;

import com.triton.msa.triton_dashboard.monitoring.dto.LogAnalysisModelUpdateRequestDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogAnalysisModelService {
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public LogAnalysisModel getAnalysisModel(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        return project.fetchEndpoint();
    }

    @Transactional
    public void updateAnalysisModel(Long projectId, LogAnalysisModelUpdateRequestDto requestDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        LogAnalysisModel endpoint = project.fetchEndpoint();
        if(endpoint != null) {
            endpoint.update(requestDto.provider(), requestDto.model());
        }
        else {
            project.updateLogAnalysisEndpoint(new LogAnalysisModel(
                    requestDto.provider(),
                    requestDto.model()
            ));
        }
    }
}
