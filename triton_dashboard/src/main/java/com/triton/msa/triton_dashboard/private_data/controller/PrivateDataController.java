package com.triton.msa.triton_dashboard.private_data.controller;

import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataResponseDto;
import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataUploadResultDto;
import com.triton.msa.triton_dashboard.private_data.service.PrivateDataPersistenceService;
import com.triton.msa.triton_dashboard.private_data.service.PrivateDataService;
import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/private-data")
@RequiredArgsConstructor
public class PrivateDataController {

    private final PrivateDataService privateDataService;
    private final PrivateDataPersistenceService privateDataPersistenceService;
    private final ProjectService projectService;

    @GetMapping
    public String listPrivateData(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);
        List<PrivateDataResponseDto> privateDataList = privateDataService.getPrivateDataList(projectId);

        model.addAttribute("project", ProjectResponseDto.from(project));
        model.addAttribute("privateDataList", privateDataList);

        return "projects/private-data";
    }

    @PostMapping("/upload")
    public String uploadZip(
            @PathVariable("projectId") Long projectId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        PrivateDataUploadResultDto result;
        result = privateDataService.unzipAndSaveFiles(projectId, file);
        redirectAttributes.addFlashAttribute("uploadResult", result);
        return "redirect:/projects/" + projectId + "/private-data";
    }

    @PostMapping("/{id}")
    public String deletePrivateData(@PathVariable Long projectId, @PathVariable Long id) {
        privateDataPersistenceService.deletePrivateData(projectId, id); // 로컬 + ES 삭제
        return "redirect:/projects/" + projectId + "/private-data";
    }
}
