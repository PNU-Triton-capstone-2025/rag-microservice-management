package com.triton.msa.triton_dashboard.project.controller;

import com.triton.msa.triton_dashboard.project.dto.ProjectCreateRequestDto;
import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping
    public String showProjectList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUser(userDetails.getUsername());
        List<ProjectResponseDto> userProjects = projectService
                .getUserProjects(user)
                .stream()
                .map(ProjectResponseDto::from)
                .toList();

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
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        projectService.createProject(requestDto, userDetails.getUsername());

        return "redirect:/projects";
    }
}
