package com.triton.msa.triton_dashboard.rag.controller;

import com.triton.msa.triton_dashboard.rag.dto.ChatPageResponseDto;
import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.rag.dto.RagRequestDto;
import com.triton.msa.triton_dashboard.rag_history.dto.RagHistoryResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.rag.util.RagExecutor;
import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/projects/{projectId}/rag")
@RequiredArgsConstructor
public class RagApiController {

    private final RagExecutor ragExecutor;
    private final ProjectService projectService;
    private final RagHistoryService ragHistoryService;

    @GetMapping
    public ResponseEntity<ChatPageResponseDto> chatPage(@PathVariable Long projectId) {
        Project project = projectService.getProject(projectId);
        List<RagHistory> historyEntities = ragHistoryService.getHistoryForProject(project);

        List<RagHistoryResponseDto> histories = historyEntities.stream()
                .map(RagHistoryResponseDto::from)
                .toList();

        ChatPageResponseDto responseDto = new ChatPageResponseDto(ProjectResponseDto.from(project), histories);

        return ResponseEntity.ok(responseDto);
    }
}
