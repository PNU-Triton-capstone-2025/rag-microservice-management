package com.triton.msa.triton_dashboard.monitoring.service;

import com.triton.msa.triton_dashboard.monitoring.dto.SavedYamlRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.SavedYamlResponseDto;
import com.triton.msa.triton_dashboard.monitoring.exception.YamlDeletionException;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.entity.SavedYaml;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final ProjectRepository projectRepository;

    @Transactional
    public void saveYaml(Long projectId, SavedYamlRequestDto requestDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));
        SavedYaml newYaml = new SavedYaml(requestDto.filename(), requestDto.yamlContent());
        project.fetchSavedYamls().add(newYaml);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<SavedYamlResponseDto> getSavedYamls(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        List<SavedYaml> savedYamls = project.fetchSavedYamls();

        return IntStream.range(0, savedYamls.size())
                .mapToObj(i -> new SavedYamlResponseDto(i, savedYamls.get(i).getFileName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteYaml(Long projectId, int yamlIndex) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));
        List<SavedYaml> savedYamls = project.fetchSavedYamls();

        if (yamlIndex >= 0 && yamlIndex < savedYamls.size()) {
            savedYamls.remove(yamlIndex);
            projectRepository.save(project);
        } else {
            throw new YamlDeletionException("해당 YAML이 존재하지 않아 삭제할 수 없습니다.");
        }
    }
}
