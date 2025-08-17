package com.triton.msa.triton_dashboard.project.controller;

import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.project.dto.ProjectCreateRequestDto;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {
    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getProjectList(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUser(userDetails.getUsername());
        List<ProjectResponseDto> projectResponseDtos = projectService.getUserProjects(user)
                .stream()
                .map(ProjectResponseDto::from)
                .toList();

        return ResponseEntity.ok(projectResponseDtos);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody ProjectCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        projectService.createProject(requestDto, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
