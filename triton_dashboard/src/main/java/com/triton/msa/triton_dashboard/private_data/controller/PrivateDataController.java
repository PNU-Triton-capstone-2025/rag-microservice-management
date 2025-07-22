package com.triton.msa.triton_dashboard.private_data.controller;

import com.triton.msa.triton_dashboard.chat.service.ChatHistoryService;
import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.private_data.service.PrivateDataService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/private-data")
@RequiredArgsConstructor
public class PrivateDataController {

    private final PrivateDataService privateDataService;
    private final ProjectService projectService;
    // private final ChatHistoryService chatHistoryService;

    @PostMapping("/upload")
    public String uploadZip(
            @PathVariable("projectId") Long projectId,
            @RequestParam("file") MultipartFile file,
            Model model
    ) {
        UploadResultDto result;

        try {
            result = privateDataService.processZipFile(projectId, file);
        } catch (IllegalArgumentException e) {
            result = new UploadResultDto(e.getMessage(), List.of(), List.of());
        }

        Project project = projectService.getProject(projectId);

        model.addAttribute("uploadResult", result);
        model.addAttribute("project", project);
        // model.addAttribute("history", chatHistoryService.getHistoryForProject(project));

        return "projects/chat";
    }
}
