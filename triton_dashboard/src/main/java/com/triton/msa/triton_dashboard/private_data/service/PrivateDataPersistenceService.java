package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadedFileResultDto;
import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import com.triton.msa.triton_dashboard.private_data.exception.PrivateDataDeleteException;
import com.triton.msa.triton_dashboard.private_data.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.private_data.util.FileTypeUtil;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrivateDataPersistenceService {

    private final PrivateDataRepository privateDataRepository;
    private final ProjectService projectService;
    @Qualifier("ragWebClient") private final WebClient ragWebClient;

    @Transactional
    public boolean saveFile(Long projectId, ExtractedFile file, List<UploadedFileResultDto> skipped) {
        if (privateDataRepository.existsByProjectIdAndFilename(projectId, file.filename())) {
            skipped.add(new UploadedFileResultDto(file.filename(), "이미 저장된 파일입니다."));
            return false;
        }

        String contentType = FileTypeUtil.resolveContentType(file.filename());
        // es 먼저 저장
        if (!saveToElasticsearch(projectId, file, contentType, skipped)) return false;
        // 그 후 로컬 db 저장
        return saveToDatabase(projectId, file, contentType, skipped);
    }

    private boolean saveToElasticsearch(Long projectId, ExtractedFile file, String contentType, List<UploadedFileResultDto> skipped) {
        String indexName = "project-" + projectId;

        Map<String, Object> document = Map.of(
                "text", file.content(),
                "file_name", file.filename(),
                "content_type", contentType,
                "es_index", indexName
        );

        try {
            ragWebClient.post()
                    .uri("/api/embedding")
                    .bodyValue(document)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return true;
        } catch (Exception e) {
            skipped.add(new UploadedFileResultDto(file.filename(), "시스템 오류로 저장되지 않았습니다."));
            return false;
        }
    }

    private boolean saveToDatabase(Long projectId, ExtractedFile file, String contentType,  List<UploadedFileResultDto> skipped) {
        Project project = projectService.getProject(projectId);
        PrivateData privateData = new PrivateData(project, file.filename(), contentType, file.timestamp());

        try {
            privateDataRepository.save(privateData);
            return true;
        } catch (Exception e) {
            deleteFromElasticsearch(projectId, file.filename());
            skipped.add(new UploadedFileResultDto(file.filename(), "시스템 오류로 저장되지 않았습니다."));
            return false;
        }
    }

    @Transactional
    public void deletePrivateData(Long projectId, Long dataId) {
        PrivateData data = privateDataRepository.findByIdAndProjectId(dataId, projectId)
                .orElseThrow(() -> new PrivateDataDeleteException("해당 데이터가 존재하지 않아 삭제할 수 없습니다."));

        // es 먼저 제거.
        deleteFromElasticsearch(projectId, data.getFilename());
        // 그 후 로컬 db 제거
        privateDataRepository.deleteById(dataId);
    }

    private void deleteFromElasticsearch(Long projectId, String filename) {
        String indexName = "project-" + projectId;

        Map<String, Object> payload = Map.of(
                "es_index", indexName,
                "file_name", filename
        );

        ragWebClient.post()
            .uri("/api/embedding/delete")
            .bodyValue(payload)
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(body -> new PrivateDataDeleteException("ES 삭제 실패(" + resp.statusCode().value() + "): " + body))
            )
            .toBodilessEntity()
            .onErrorMap(ex -> new PrivateDataDeleteException("ES 연결/요청 실패: " + ex.getMessage()))
            .block();
    }
}
