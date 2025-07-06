package com.triton.msa.triton_dashboard.project.service;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import com.triton.msa.triton_dashboard.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<Project> getUserProjects(User user) {
        return projectRepository.findByUser(user);
    }

    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    public Project getProject(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));
    }
}
