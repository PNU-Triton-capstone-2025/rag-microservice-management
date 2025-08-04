package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.private_data.util.FileTypeUtil;
import com.triton.msa.triton_dashboard.private_data.util.ZipExtractor;
import com.triton.msa.triton_dashboard.project.entity.PrivateData;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.ConnectException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivateDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ProjectService projectService;
    private final PrivateDataRepository privateDataRepository;
    private final ZipExtractor zipExtractor;
    private final HttpHeaders headers = new HttpHeaders();

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
                String reason = "";
                if (FileTypeUtil.isAllowed(doc.filename())) {
                    try {
                        saveFile(projectId, doc);
                        saved.add(doc.filename());
                    } catch (ConnectException e) {
                        reason = "(저장 실패: 서버에 연결할 수 없음)";
                    } catch (Exception e) {
                        reason = "(저장 실패: 알 수 없는 오류)";
                    }
                } else {
                    skipped.add(doc.filename() + " (허용되지 않음)");
                }

                if (!reason.isEmpty()) {
                    skipped.add(doc.filename() + " " + reason);
                }
            }

            return new UploadResultDto("업로드 완료", saved, skipped);

        } catch (IOException e) {
            return new UploadResultDto("압축 해제 실패: " + e.getMessage(), List.of(), List.of());
        }
    }

    private void saveFile(Long projectId, ExtractedFile file) throws ConnectException {
        String contentType = FileTypeUtil.resolveContentType(file.filename());
        String esId;
        try {
            esId = saveToElasticsearch(projectId, file, contentType);
        } catch (Exception e) {
            throw new ConnectException("Elasticsearch 저장 중 오류 발생: " + e.getMessage());
        }

        if (esId != null && !esId.isBlank()) {
            saveToDatabase(projectId, file, contentType, esId);
        }
    }

    private void saveToDatabase(Long projectId, ExtractedFile file, String contentType, String esId) {
        Project project = projectService.getProject(projectId);
        PrivateData privateData = new PrivateData();

        privateData.setProject(project);
        privateData.setFilename(file.filename());
        privateData.setContentType(contentType);
        privateData.setEsId(esId);
        privateData.setCreatedAt(file.timestamp());

        privateDataRepository.save(privateData);
    }

    private String saveToElasticsearch(Long projectId, ExtractedFile file, String contentType) {
        // String indexUrl = "http://54.253.214.14:9200/project-" + projectId + "/_doc";
        String indexUrl = "http://localhost:30920/project-" + projectId + "/_doc";

        Map<String, Object> document = Map.of(
                "filename", file.filename(),
                "contentType", contentType,
                "content", file.content(),
                "timestamp", file.timestamp().toString()
        );

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(document, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(indexUrl, request, Map.class);

        String esId = Optional.ofNullable(response.getBody())
                .map(b -> b.get("_id"))
                .map(Object::toString)
                .orElseThrow(() -> new RuntimeException("Elasticsearch 저장 실패: _id 없음"));

        return esId;
    }

    public void deletePrivateData(Long projectId, Long dataId) {
        PrivateData data = privateDataRepository.findByIdAndProjectId(dataId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 데이터가 존재하지 않습니다."));

        deleteFromElasticsearch(projectId, data.getEsId());
        privateDataRepository.deleteById(dataId);
    }

    private void deleteFromElasticsearch(Long projectId, String esId) {
        String indexUrl = "http://localhost:30920/project-" + projectId + "/_doc/" + esId;
        restTemplate.delete(indexUrl);
    }

    public List<PrivateData> getPrivateDataList(Long projectId) {
        return privateDataRepository.findByProjectId(projectId);
    }
}