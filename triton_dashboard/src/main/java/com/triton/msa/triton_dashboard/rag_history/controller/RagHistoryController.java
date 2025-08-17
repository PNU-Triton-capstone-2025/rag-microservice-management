package com.triton.msa.triton_dashboard.rag_history.controller;

import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.rag_history.dto.RagHistoryResponseDto;
import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/rag/history")
@RequiredArgsConstructor
public class RagHistoryController {

    private final RagHistoryService ragHistoryService;
    private final ProjectService projectService;

    @GetMapping
    public String chatHistoryList(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);
        List<RagHistoryResponseDto> histories = ragHistoryService.getHistoryForProject(project)
                .stream()
                .map(RagHistoryResponseDto::from)
                .toList();

        model.addAttribute("project", ProjectResponseDto.from(project));
        model.addAttribute("histories", histories);

        return "projects/rag-history-list";
    }

    @GetMapping("/{historyId}")
    public String chatHistoryDetail(@PathVariable Long projectId, @PathVariable Long historyId, Model model) {
        Project project = projectService.getProject(projectId);
        RagHistory history = ragHistoryService.getHistoryById(historyId);

        model.addAttribute("project", ProjectResponseDto.from(project));
        model.addAttribute("history", RagHistoryResponseDto.from(history));

        return "projects/rag-history-detail";
    }

    @PostMapping("/{historyId}/delete")
    public String deleteHistory(@PathVariable Long historyId, @PathVariable Long projectId) {
        ragHistoryService.deleteHistory(historyId, projectId);

        return "redirect:/projects/" + projectId + "/rag/history";
    }
}