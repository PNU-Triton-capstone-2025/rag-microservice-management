package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.project.entity.PrivateData;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ProjectService projectService;
    private final PrivateDataRepository privateDataRepository;
    private final Tika tika = new Tika();

    public UploadResultDto unzipAndSaveFiles(Long projectId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            return new UploadResultDto("지원되지 않는 파일 형식입니다. .zip 파일만 업로드해주세요.", List.of(), List.of());
        }

        Path tempDir = null;
        List<String> saved = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        try {
            tempDir = Files.createTempDirectory("upload-zip");
            List<ExtractedFile> extractedFiles = unzipToFileList(file.getInputStream(), tempDir, skipped);

            for (ExtractedFile doc : extractedFiles) {
                String reason = "";

                if (isAllowed(doc.filename())) {
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
        } finally {
            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException e) {
                    // 로그만 남기고 무시
                    System.err.println("임시 디렉토리 삭제 실패: " + e.getMessage());
                }
            }
        }
    }

    private List<ExtractedFile> unzipToFileList(InputStream inputStream, Path targetDir, List<String> skipped) throws IOException {
        List<ExtractedFile> files = new ArrayList<>();

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, "CP949", true)) {
            ZipArchiveEntry entry;

            while ((entry = zis.getNextZipEntry()) != null) {
                if (entry.isDirectory()) continue;

                String filename = entry.getName();
                Path newFile = targetDir.resolve(filename).normalize();

                if (!newFile.startsWith(targetDir)) {
                    throw new IOException("Zip Slip 공격 탐지됨");
                }

                Files.createDirectories(newFile.getParent());
                Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
              
                try {
                    String content;
                    if (isPlainText(filename)) {
                        content = Files.readAllLines(newFile, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
                    } else {
                        content = tika.parseToString(newFile);
                    }

                    if (content == null || content.isBlank()) {
                        skipped.add(filename + " (추출된 데이터 없음)");
                        continue;
                    }

                    content.getBytes(StandardCharsets.UTF_8); // 인코딩 검증
                    files.add(new ExtractedFile(filename, content, Instant.now()));
                } catch (IOException | TikaException e) {
                    skipped.add(filename + " (텍스트 추출 실패: UTF-8 인코딩 오류)");
                }
            }
        }

        return files;
    }


    private boolean isPlainText(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".log") || lower.endsWith(".env") ||
                lower.endsWith(".ini") || lower.endsWith(".conf") || lower.endsWith(".csv") ||
                lower.endsWith(".md") || lower.endsWith(".yaml") || lower.endsWith(".yml");
    }

    private boolean isAllowed(String filename) {
        String lower = filename.toLowerCase();
        return !(lower.endsWith(".exe") || lower.endsWith(".sh") || lower.endsWith("bat"));
    }

    private String resolveContentType(String filename) {
        String lower = filename.toLowerCase();

        if (isPlainText(filename)) return "text/plain";

        if (lower.endsWith(".md")) return "text/markdown";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) return "application/x-yaml";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".xml")) return "application/xml";

        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

        return "application/octet-stream";
    }

    private void saveToDatabase(Long projectId, ExtractedFile file, String contentType) {
        Project project = projectService.getProject(projectId);
        PrivateData privateData = new PrivateData();

        privateData.setProject(project);
        privateData.setFilename(file.filename());
        privateData.setContentType(contentType);
        privateData.setData(file.content().getBytes(StandardCharsets.UTF_8));
        privateData.setCreatedAt(file.timestamp());

        privateDataRepository.save(privateData);
    }

    private void saveToElasticsearch(Long projectId, ExtractedFile file, String contentType) {
        // String indexUrl = "http://54.253.214.14:9200/project-" + projectId + "/_doc";
        String indexUrl = "http://localhost:30920/project-" + projectId + "/_doc";

        Map<String, Object> documnet = Map.of(
                "filename", file.filename(),
                "contentType", contentType,
                "content", file.content(),
                "timestamp", file.timestamp().toString()
        );

        restTemplate.postForEntity(indexUrl, documnet, String.class);
    }

    private void saveFile(Long projectId, ExtractedFile file) throws ConnectException {
        String contentType = resolveContentType(file.filename());
        try {
            saveToElasticsearch(projectId, file, contentType);
        } catch (Exception e) {
            throw new ConnectException("Elasticsearch 저장 중 오류 발생: " + e.getMessage());
        }

        saveToDatabase(projectId, file, contentType);
    }

    public List<PrivateData> getPrivateDataList(Long projectId) {
        return privateDataRepository.findByProjectId(projectId);
    }

    public void deletePrivateData(Long projectId, Long dataId) {
        PrivateData data = privateDataRepository.findByIdAndProjectId(dataId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 데이터가 존재하지 않습니다."));

        String indexUrl = "http://localhost:30920/project-" + projectId + "/_delete_by_query";
        String query = """
            {
              "query": {
                "match": {
                  "filename": "%s"
                }
              }
            }
            """.formatted(data.getFilename());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(query, headers);

        restTemplate.postForEntity(indexUrl, request, String.class);
        privateDataRepository.deleteById(dataId);
    }
}