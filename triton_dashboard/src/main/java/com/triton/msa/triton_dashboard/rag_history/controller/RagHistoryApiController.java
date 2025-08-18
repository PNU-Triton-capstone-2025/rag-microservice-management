package com.triton.msa.triton_dashboard.rag_history.controller;

import com.triton.msa.triton_dashboard.rag_history.dto.RagHistoryResponseDto;
import com.triton.msa.triton_dashboard.rag_history.dto.RagHistorySaveRequestDto;
import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/projects/{projectId}/rag/history")
@RequiredArgsConstructor
public class RagHistoryApiController {

    private final RagHistoryService ragHistoryService;
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<RagHistoryResponseDto>> getChatHistoryList(@PathVariable Long projectId) {
        Project project = projectService.getProject(projectId);
        List<RagHistoryResponseDto> historyResponseDtos = ragHistoryService.getHistoryForProject(project)
                .stream()
                .map(RagHistoryResponseDto::from)
                .toList();

        return ResponseEntity.ok(historyResponseDtos);
    }

    @PostMapping
    public ResponseEntity<?> save(@PathVariable Long projectId,
                                  @Valid @RequestBody RagHistorySaveRequestDto dto) {
        Project project = projectService.getProject(projectId);
        Long savedId = ragHistoryService.saveHistory(project, dto.userQuery(), dto.llmResponse());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", savedId));
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<RagHistoryResponseDto> getChatHistoryDetail(@PathVariable Long historyId) {
        RagHistory history = ragHistoryService.getHistoryById(historyId);
        RagHistoryResponseDto historyResponseDto = RagHistoryResponseDto.from(history);

        return ResponseEntity.ok(historyResponseDto);
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long historyId, @PathVariable Long projectId) {
        ragHistoryService.deleteHistory(historyId, projectId);
        return ResponseEntity.noContent().build();
    }
}