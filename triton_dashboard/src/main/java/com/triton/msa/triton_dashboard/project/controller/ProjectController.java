package com.triton.msa.triton_dashboard.project.controller;

import com.triton.msa.triton_dashboard.private_data.service.PrivateDataService;
import com.triton.msa.triton_dashboard.project.dto.ProjectCreateRequestDto;
import com.triton.msa.triton_dashboard.project.entity.PrivateData;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.service.UserService;
import com.triton.msa.triton_dashboard.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final PrivateDataService privateDataService;

    @GetMapping
    public String showProjectList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUser(userDetails.getUsername());
        List<Project> userProjects = projectService.getUserProjects(user);

        model.addAttribute("projects", userProjects);

        return "projects/list";
    }

    @GetMapping("/new")
    public String newProjectForm(Model model) {
        model.addAttribute("newProject", ProjectCreateRequestDto.getEmpty());
        return "projects/form";
    }

    @PostMapping
    public String createProject(
            @ModelAttribute("newProject") ProjectCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        projectService.createProject(requestDto, userDetails.getUsername());

        return "redirect:/projects";
    }

    @GetMapping("/{projectId}/private-data")
    public String listPrivateData(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);
        List<PrivateData> privateDataList = privateDataService.getPrivateDataList(projectId);

        model.addAttribute("project", project);
        model.addAttribute("privateDataList", privateDataList);

        return "projects/private-data";
    }

    @DeleteMapping("/{projectId}/private-data/{id}")
    public String deletePrivateData(@PathVariable Long projectId, @PathVariable Long id) {
        privateDataService.deletePrivateData(projectId, id); // 로컬 + ES 삭제
        return "redirect:/projects/" + projectId + "/private-data";
    }
}
