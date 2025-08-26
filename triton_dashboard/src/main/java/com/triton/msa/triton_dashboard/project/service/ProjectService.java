package com.triton.msa.triton_dashboard.project.service;

import com.triton.msa.triton_dashboard.project.dto.ProjectCreateRequestDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import com.triton.msa.triton_dashboard.ssh.dto.SshInfoCreateRequestDto;
import com.triton.msa.triton_dashboard.ssh.entity.SshInfo;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<Project> getUserProjects(User user) {
        return projectRepository.findByUser(user);
    }

    @Transactional
    public void createProject(ProjectCreateRequestDto requestDto, String username) {
        SshInfo sshInfo = buildSshInfo(requestDto.sshInfoCreateRequestDto());

        Project project = new Project(requestDto.name());
        project.updateSshInfo(sshInfo);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        user.addProject(project);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));
    }

    @Transactional(readOnly = true)
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    private SshInfo buildSshInfo(SshInfoCreateRequestDto requestDto) {
        String pemKeyContent = makeStringPemKey(requestDto.pemFile());

        return new SshInfo(
                requestDto.sshIpAddress(),
                requestDto.username(),
                pemKeyContent
        );
    }

    private String makeStringPemKey(MultipartFile pemFile) {
        String pemKeyContent = "";
        if(pemFile == null || pemFile.isEmpty()) {
            return pemKeyContent;
        }

        try{
            pemKeyContent = new String(pemFile.getBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            log.error("Failed to read pem file", ex);
        }
        return pemKeyContent;
    }
}
