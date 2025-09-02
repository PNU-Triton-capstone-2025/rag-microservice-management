package com.triton.msa.triton_dashboard.monitoring.controller;

import com.triton.msa.triton_dashboard.monitoring.dto.SavedYamlRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.SavedYamlResponseDto;
import com.triton.msa.triton_dashboard.monitoring.entity.MonitoringHistory;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringHistoryService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringService;
import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final MonitoringHistoryService monitoringHistoryService;
    private final ProjectService projectService;

    @GetMapping
    public String monitoringPage(@PathVariable("projectId") Long projectId,
                                 @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                 Model model) {
        ProjectResponseDto projectDto = ProjectResponseDto.from(projectService.getProject(projectId));
        List<SavedYamlResponseDto> savedYamls = monitoringService.getSavedYamls(projectId);
        Page<MonitoringHistory> monitoringHistories = monitoringHistoryService.getMonitoringHistories(projectId, pageable);

        model.addAttribute("project", projectDto);
        model.addAttribute("savedYamls", savedYamls);
        model.addAttribute("monitoringHistories", monitoringHistories);

        return "projects/monitoring";
    }

    @PostMapping("/upload")
    public String uploadYamls(@PathVariable("projectId") Long projectId,
                              @RequestParam("yamlFiles") MultipartFile[] files,
                              RedirectAttributes redirectAttributes) {

        monitoringService.saveYamls(projectId, files);
        redirectAttributes.addFlashAttribute("successMessage", "파일(총 " + files.length + "개)이 성공적으로 저장되었습니다.");

        return "redirect:/projects/" + projectId + "/monitoring";
    }

    @PostMapping("/delete/{yamlIndex}")
    public String deleteYaml(@PathVariable("projectId") Long projectId,
                             @PathVariable("yamlIndex") int yamlIndex,
                             RedirectAttributes redirectAttributes) {

        monitoringService.deleteYaml(projectId, yamlIndex);
        redirectAttributes.addFlashAttribute("successMessage", "파일이 성공적으로 삭제되었습니다.");

        return "redirect:/projects/" + projectId + "/monitoring";
    }
}
