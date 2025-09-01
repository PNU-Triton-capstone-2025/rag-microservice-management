package com.triton.msa.triton_dashboard.rag.controller;

import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.rag.dto.RagRequestDto;
import com.triton.msa.triton_dashboard.rag.util.RagExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.print.attribute.standard.Media;

@Controller
@RequestMapping("/projects/{projectId}/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagExecutor ragExecutor;
    private final ProjectService projectService;

    @GetMapping
    public String ragPage(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);
        model.addAttribute("project", ProjectResponseDto.from(project));

        return "projects/rag";
    }

    @PostMapping(value = "/stream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamChatResponse(@PathVariable Long projectId,
                                           @RequestPart("requestDto") RagRequestDto requestDto,
                                           @RequestPart(value = "file", required = false)MultipartFile file) {
        return ragExecutor.streamChatResponse(projectId, requestDto, file);
    }
}