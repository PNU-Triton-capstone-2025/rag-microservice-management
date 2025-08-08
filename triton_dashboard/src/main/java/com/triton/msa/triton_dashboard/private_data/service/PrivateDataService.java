package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataResponseDto;
import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.private_data.exception.ElasticsearchDeleteException;
import com.triton.msa.triton_dashboard.private_data.util.FileTypeUtil;
import com.triton.msa.triton_dashboard.private_data.util.ZipExtractor;
import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.private_data.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.ConnectException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrivateDataService {

    private final ProjectService projectService;
    private final PrivateDataRepository privateDataRepository;
    private final ZipExtractor zipExtractor;

    private final WebClient webClient;

    @Transactional
    public UploadResultDto unzipAndSaveFiles(Long projectId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            return new UploadResultDto("지원되지 않는 파일 형식입니다. .zip 파일만 업로드해주세요.", List.of(), List.of());
        }

        List<String> saved = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        try {
            List<ExtractedFile> extractedFiles = zipExtractor.extract(file, skipped);

            for (ExtractedFile doc : extractedFiles) {
                if (FileTypeUtil.isAllowed(doc.filename())) {
                    boolean isSuccess = saveFile(projectId, doc, skipped);
                    if (isSuccess) saved.add(doc.filename());
                } else {
                    skipped.add(doc.filename() + " (허용되지 않음)");
                }
            }

            return new UploadResultDto("업로드 완료", saved, skipped);

        } catch (IOException e) {
            return new UploadResultDto("압축 해제 실패: " + e.getMessage(), List.of(), List.of());
        }
    }

    private boolean saveFile(Long projectId, ExtractedFile file,  List<String> skipped) {
        if (privateDataRepository.existsByProjectIdAndFilename(projectId, file.filename())) {
            skipped.add(file.filename() + " (이미 저장된 파일)");
            return false;
        }

        String contentType = FileTypeUtil.resolveContentType(file.filename());
        try {
            saveToElasticsearch(projectId, file, contentType);
            saveToDatabase(projectId, file, contentType);
            return true;
        } catch (Exception e) {
            skipped.add(file.filename() + " (시스템 오류로 저장되지 않았습니다.)");
            return false;
        }
    }

    private void saveToDatabase(Long projectId, ExtractedFile file, String contentType) {
        Project project = projectService.getProject(projectId);
        PrivateData privateData = new PrivateData(project, file.filename(), contentType, file.timestamp());

        privateDataRepository.save(privateData);
    }

    private void saveToElasticsearch(Long projectId, ExtractedFile file, String contentType) {
        String url = "http://localhost:30920/project-" + projectId + "/_doc";
        Map<String, Object> document = Map.of(
                "filename", file.filename(),
                "contentType", contentType,
                "content", file.content(),
                "timestamp", file.timestamp().toString()
        );

        webClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(document)
                .retrieve()
                .bodyToMono(Void.class)
                .block(); // block으로 하면 응답에 의존적인 것 같은데, subcribe로 바꾸는 게 맞는지
    }

    public void deletePrivateData(Long projectId, Long dataId) {
        PrivateData data = privateDataRepository.findByIdAndProjectId(dataId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 데이터가 존재하지 않습니다."));

        deleteFromElasticsearch(projectId, data.filename());
        privateDataRepository.deleteById(dataId);
    }

    private void deleteFromElasticsearch(Long projectId, String filename) {
        String url = "http://localhost:30920/project-" + projectId + "/_delete_by_query";
        String deleteQuery = """
            {
                "query": {
                    "term": {
                        "filename.keyword": "%s"
                    }
                }
            }
        """.formatted(filename);

        try {
            webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(deleteQuery)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(); // block으로 하면 응답에 의존적인 것 같은데, subcribe로 바꾸는 게 맞는지
        } catch (Exception e) {
            throw new ElasticsearchDeleteException("서버 연결 오류로 삭제하지 못했습니다.");
        }

    }

    @Transactional(readOnly = true)
    public List<PrivateDataResponseDto> getPrivateDataList(Long projectId) {
        return privateDataRepository.findDtosByProjectId(projectId);
    }
}