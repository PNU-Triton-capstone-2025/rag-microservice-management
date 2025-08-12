package com.triton.msa.triton_dashboard.chat.controller;

import com.triton.msa.triton_dashboard.chat.entity.ChatHistory;
import com.triton.msa.triton_dashboard.chat.service.ChatHistoryService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects/{projectId}/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;
    private final ProjectService projectService;

    @GetMapping
    public String chatHistoryList(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);

        model.addAttribute("project", project);
        model.addAttribute("histories", chatHistoryService.getHistoryForProject(project));

        return "projects/chat-history-list";
    }

    @GetMapping("/{historyId}")
    public String chatHistoryDetail(@PathVariable Long projectId, @PathVariable Long historyId, Model model) {
        Project project = projectService.getProject(projectId);
        ChatHistory history = chatHistoryService.getHistoryById(historyId);

        model.addAttribute("project", project);
        model.addAttribute("history", history);

        return "projects/chat-history-detail";
    }

    @PostMapping("/{historyId}/delete")
    public String deleteHistory(@PathVariable Long historyId, @PathVariable Long projectId) {
        chatHistoryService.deleteHistory(historyId, projectId);

        return "redirect:/projects/" + projectId + "/chat/history";
    }
}