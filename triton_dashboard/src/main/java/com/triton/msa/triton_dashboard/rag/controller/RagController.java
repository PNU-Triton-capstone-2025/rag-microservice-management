package com.triton.msa.triton_dashboard.rag.controller;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.rag.util.RagExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Controller
@RequestMapping("/projects/{projectId}/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagExecutor ragExecutor;
    private final ProjectService projectService;

    @GetMapping
    public String ragPage(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);
        model.addAttribute("project", project);

        return "projects/rag";
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamChatResponse(@PathVariable Long projectId, @RequestParam String query) {
        return ragExecutor.streamChatResponse(projectId, query);
    }
}