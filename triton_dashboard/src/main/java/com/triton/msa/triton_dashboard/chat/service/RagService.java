package com.triton.msa.triton_dashboard.chat.service;

import com.triton.msa.triton_dashboard.chat.dto.RagRequestDto;
import com.triton.msa.triton_dashboard.chat.dto.RagResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RagService {

    private final RestTemplate restTemplate;
    private final ChatHistoryService chatHistoryService;
    private final ProjectService projectService;

    @Value("${rag.service.url}")
    private String ragServiceUrl;

    public RagResponseDto generateDeploymentSpec(String username, Long projectId, String query) {
        Project project = projectService.getProject(projectId);

        RagRequestDto requestDto = new RagRequestDto(query);

        RagResponseDto responseDto = restTemplate.postForObject(ragServiceUrl, requestDto, RagResponseDto.class);

        if(responseDto != null) {
            chatHistoryService.saveHistory(project, query, responseDto.response());
        }

        return responseDto;
    }
}
