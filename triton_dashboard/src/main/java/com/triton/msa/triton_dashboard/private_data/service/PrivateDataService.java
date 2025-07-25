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
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
            throw new IllegalArgumentException("지원되지 않는 파일 형식입니다. .zip 파일만 업로드해주세요.");
        }

        try {
            // 압축 해제를 위한 임시 디렉토리 생성
            Path tempDir = Files.createTempDirectory("upload-zip");
            List<ExtractedFile> extractedFiles = unzipToFileList(file.getInputStream(), tempDir);

            List<String> saved = new ArrayList<>();
            List<String> skipped = new ArrayList<>();

            for (ExtractedFile doc : extractedFiles) {
                if (isAllowed(doc.filename())) {
                    try {
                        saveFile(projectId, doc);
                        saved.add(doc.filename());
                    } catch (Exception e) {
                        String reason;
                        if (e.getMessage().contains("Connection refused")) {
                            reason = "(저장 실패: 서버에 연결할 수 없음)";
                        } else {
                            reason = "(저장 실패: 알 수 없는 오류)";
                        }
                        skipped.add(doc.filename() + " " + reason);
                    }
                } else {
                    skipped.add(doc.filename() + " (허용되지 않음)");
                }
            }

            return new UploadResultDto("업로드 완료", saved, skipped);

        } catch (IOException e) {
            return new UploadResultDto("압축 해제 실패: " + e.getMessage(), List.of(), List.of());
        }
    }

    private List<ExtractedFile> unzipToFileList(InputStream inputStream, Path targetDir) throws IOException {
        List<ExtractedFile> files = new ArrayList<>();

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, "CP949", true)) {
            ZipArchiveEntry entry;

            while ((entry = zis.getNextZipEntry()) != null) {
                if (entry.isDirectory()) continue;

                String filename = entry.getName(); // 한글 포함된 파일명도 CP949로 정상 복호화
                Path newFile = targetDir.resolve(filename).normalize();

                if (!newFile.startsWith(targetDir)) {
                    throw new IOException("Zip Slip 공격 탐지됨");
                }

                Files.createDirectories(newFile.getParent());
                Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);

                String content;
                try {
                    if (isPlainText(filename)) {
                        content = Files.readAllLines(newFile, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
                    } else {
                        content = extractTextFromFile(newFile);
                    }

                    content.getBytes(StandardCharsets.UTF_8); // 인코딩 검증
                } catch (Exception e) {
                    content = "텍스트 추출 실패 (UTF-8 인코딩 오류)";
                }

                files.add(new ExtractedFile(filename, content, Instant.now()));
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

    private String extractTextFromFile(Path filePath) {
        try {
            return tika.parseToString(filePath);
        } catch (IOException | TikaException e) {
            return "텍스트 추출 실패";
        }
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

        privateDataRepository.save(privateData);
    }

    private void saveToElasticsearch(Long projectId, ExtractedFile file, String contentType) {
        // String indexUrl = "http://54.253.214.14:9200/project-" + projectId + "/_doc";
        String indexUrl = "http://localhost:30920/project-" + projectId + "/_doc";

        Map<String, Object> documnet = Map.of(
                "filename", file.filename(),
                "content", file.content(),
                "timestamp", file.timestamp().toString(),
                "contentType", contentType
        );

        restTemplate.postForEntity(indexUrl, documnet, String.class);
    }

    private void saveFile(Long projectId, ExtractedFile file) {
        String contentType = resolveContentType(file.filename());
        try {
            saveToElasticsearch(projectId, file, contentType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        // Elasticsearch에 저장 성공 시에만 로컬 db도 저장
        saveToDatabase(projectId, file, contentType);
    }
}

