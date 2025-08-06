package com.triton.msa.triton_dashboard.ssh.controller;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.ssh.service.SshService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ssh")
public class SshController {
    private final SshService sshService;
    private final ProjectService projectService;

    public SshController(SshService sshService, ProjectService projectService) {
        this.sshService = sshService;
        this.projectService = projectService;
    }

    @PostMapping("/connect/{projectId}")
    public ResponseEntity<Map<String, String>> initiateSshConnection(@PathVariable Long projectId) {
        Project project = projectService.getProject(projectId);

        String sessionId = sshService.startSshSession(project.getSshInfo());

        return ResponseEntity.ok(Map.of("sessionID", sessionId));
    }
}
