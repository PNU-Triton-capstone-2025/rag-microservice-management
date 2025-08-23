package com.triton.msa.triton_dashboard.monitoring.service;

import com.triton.msa.triton_dashboard.monitoring.dto.LogAnalysisEndpointUpdateRequestDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisEndpoint;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogAnalysisEndpointService {
    ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public LogAnalysisEndpoint getEndpoint(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        return project.fetchEndpoint();
    }

    @Transactional
    public void updateEndpoint(Long projectId, LogAnalysisEndpointUpdateRequestDto requestDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        LogAnalysisEndpoint endpoint = project.fetchEndpoint();

        if(endpoint != null) {
            endpoint.update(requestDto.provider(), requestDto.model());
        }
        else {
            project.updateLogAnalysisEndpoint(new LogAnalysisEndpoint(
                    requestDto.provider(),
                    requestDto.model()
            ));
        }
    }
}
