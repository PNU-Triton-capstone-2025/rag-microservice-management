package com.triton.msa.triton_dashboard.monitoring.controller;

import com.triton.msa.triton_dashboard.monitoring.dto.SavedYamlRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.SavedYamlResponseDto;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringService;
import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
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
    private final ProjectService projectService;

    @GetMapping
    public String monitoringPage(@PathVariable("projectId") Long projectId, Model model) {
        ProjectResponseDto project = ProjectResponseDto.from(projectService.getProject(projectId));
        List<SavedYamlResponseDto> savedYamls = monitoringService.getSavedYamls(projectId);

        model.addAttribute("project", project);
        model.addAttribute("savedYamls", savedYamls);

        return "projects/monitoring";
    }

    @PostMapping("/upload")
    public String uploadYaml(@PathVariable("projectId") Long projectId,
                             @RequestParam("yamlFiles")MultipartFile[] files,
                             RedirectAttributes redirectAttributes) {

        if (files.length == 0 || Arrays.stream(files).allMatch(MultipartFile::isEmpty)) {
            redirectAttributes.addFlashAttribute("errorMessage", "파일을 하나 이상 선택해주세요.");
            return "redirect:/projects/" + projectId + "/monitoring";
        }

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String filename = file.getOriginalFilename();
                String yamlContent = new String(file.getBytes(), StandardCharsets.UTF_8);

                monitoringService.saveYaml(projectId, new SavedYamlRequestDto(filename, yamlContent));
            }

            redirectAttributes.addFlashAttribute("successMessage", "파일(총 " + files.length + "개)이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "파일을 읽는 중 오류가 발생했습니다.");
        }

        return "redirect:/projects/" + projectId + "/monitoring";
    }

    @PostMapping("delete/{yamlIndex}")
    public String deleteYaml(@PathVariable("projectId") Long projectId,
                             @PathVariable("yamlIndex") int yamlIndex,
                             RedirectAttributes redirectAttributes) {

        try {
            monitoringService.deleteYaml(projectId, yamlIndex);
            redirectAttributes.addFlashAttribute("successMessage", "파일이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/projects/" + projectId + "/monitoring";
    }
}
