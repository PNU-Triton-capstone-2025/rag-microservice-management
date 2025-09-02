package com.triton.msa.triton_dashboard.monitoring.service;

import com.triton.msa.triton_dashboard.monitoring.dto.SavedYamlResponseDto;
import com.triton.msa.triton_dashboard.monitoring.exception.YamlFileException;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.entity.SavedYaml;
import com.triton.msa.triton_dashboard.project.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final ProjectRepository projectRepository;

    @Transactional
    public void saveYamls(Long projectId, MultipartFile[] files) {
        if (files == null || files.length == 0 || Arrays.stream(files).allMatch(MultipartFile::isEmpty)) {
            throw new YamlFileException("업로드할 파일을 하나 이상 선택해주세요.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".yaml") && !filename.endsWith(".yml"))) {
                throw new YamlFileException("YAML 파일(.yml, .yaml)만 업로드할 수 있습니다. 잘못된 파일: " + filename);
            }

            try {
                String yamlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
                SavedYaml newYaml = new SavedYaml(filename, yamlContent);
                project.fetchSavedYamls().add(newYaml);
            } catch (IOException e) {
                throw new YamlFileException("YAML 파일의 내용을 읽어올 수 없습니다: " + file.getOriginalFilename());
            }
        }
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<SavedYaml> getSavedYamlsWithContent(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        return project.fetchSavedYamls();
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
            throw new YamlFileException("해당 YAML을 삭제할 수 없습니다.");
        }
    }
}
