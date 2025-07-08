package com.triton.msa.triton_dashboard.chat.controller;

import com.triton.msa.triton_dashboard.chat.dto.RagResponseDto;
import com.triton.msa.triton_dashboard.chat.service.ChatHistoryService;
import com.triton.msa.triton_dashboard.chat.service.RagService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/projects/{projectId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;
    private final ChatHistoryService chatHistoryService;
    private final ProjectService projectService;

    @GetMapping
    public String chatPage(@PathVariable Long projectId, Model model, Principal principal) {
        Project project = projectService.getProject(projectId);

        model.addAttribute("project", project);
        model.addAttribute("history", chatHistoryService.getHistoryForProject(project));

        return "projects/chat";
    }

    @PostMapping("/send")
    public String sendMessage(@PathVariable Long projectId, @RequestParam String query, Model model, Principal principal) {
        Project project = projectService.getProject(projectId);

        RagResponseDto responseDto = ragService.generateDeploymentSpec(principal.getName(), projectId, query);

        model.addAttribute("project", project);
        model.addAttribute("response", responseDto);
        model.addAttribute("query", query);

        model.addAttribute("history", chatHistoryService.getHistoryForProject(project));

        return "projects/chat";
    }
}
