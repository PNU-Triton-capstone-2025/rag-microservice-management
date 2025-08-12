package com.triton.msa.triton_dashboard.chat.controller;

import com.triton.msa.triton_dashboard.chat.dto.RagHistoryResponseDto;
import com.triton.msa.triton_dashboard.chat.dto.ChatPageResponseDto;
import com.triton.msa.triton_dashboard.chat.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.rag.service.RagService;
import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
@RequiredArgsConstructor
public class ChatApiController {

    private final RagService ragService;
    private final RagHistoryService ragHistoryService;
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ChatPageResponseDto> chatPage(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);
        List<RagHistory> historyEntities = ragHistoryService.getHistoryForProject(project);

        ProjectResponseDto projectResponseDto = new ProjectResponseDto(project.getId(), project.getName());
        List<RagHistoryResponseDto> histories = historyEntities.stream()
                .map(RagHistoryResponseDto::from)
                .toList();

        ChatPageResponseDto responseDto = new ChatPageResponseDto(projectResponseDto, histories);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatResponse(@PathVariable Long projectId, @RequestParam String query) {
        return ragService.streamChatResponse(projectId, query);
    }

    @GetMapping("/history")
    public ResponseEntity<List<RagHistoryResponseDto>> getChatHistoryList(@PathVariable Long projectId) {
        Project project = projectService.getProject(projectId);
        List<RagHistoryResponseDto> historyResponseDtos = ragHistoryService.getHistoryForProject(project)
                .stream()
                .map(RagHistoryResponseDto::from)
                .toList();

        return ResponseEntity.ok(historyResponseDtos);
    }

    @GetMapping("/history/{historyId}")
    public ResponseEntity<RagHistoryResponseDto> getChatHistoryDetail(@PathVariable Long historyId) {
        RagHistory history = ragHistoryService.getHistoryById(historyId);
        RagHistoryResponseDto historyResponseDto = RagHistoryResponseDto.from(history);

        return ResponseEntity.ok(historyResponseDto);
    }

    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long historyId, @PathVariable Long projectId) {
        ragHistoryService.deleteHistory(historyId, projectId);
        return ResponseEntity.noContent().build();
    }
}
