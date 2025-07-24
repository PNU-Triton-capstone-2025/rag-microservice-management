package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.project.entity.PrivateData;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class PrivateDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ProjectService projectService;
    private final PrivateDataRepository privateDataRepository;

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

        try (ZipInputStream zis = new ZipInputStream((inputStream))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                // 폴더는 스킵
                if (entry.isDirectory()) continue;

                // 압축 해제 대상 경로 구성
                Path newFile = targetDir.resolve(entry.getName()).normalize();
                if (!newFile.startsWith(targetDir)) throw new IOException("Zip Slip 공격 탑지됨");

                Files.createDirectories(newFile.getParent());
                Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);

                // Elasticsearch 저장을 위해 파일 내용을 하나의 문자열로 합침
                String content = Files.readAllLines(newFile).stream().collect(Collectors.joining("\n"));
                files.add(new ExtractedFile(entry.getName(), content, Instant.now()));
            }
        }
        return files;
    }

    private boolean isAllowed(String filename) {
        String lower = filename.toLowerCase();
        return !(lower.endsWith(".exe") || lower.endsWith(".sh") || lower.endsWith("bat"));
    }

    private void saveToDatabase(Long projectId, ExtractedFile file) {
        Project project = projectService.getProject(projectId);
        PrivateData privateData = new PrivateData();

        privateData.setProject(project);
        privateData.setFilename(file.filename());
        privateData.setContentType("text/plain");
        privateData.setData(file.content().getBytes());

        privateDataRepository.save(privateData);
    }

    private void saveToElasticsearch(Long projectId, ExtractedFile file) {
        // String indexUrl = "http://54.253.214.14:9200/project-" + projectId + "/_doc";
        String indexUrl = "http://localhost:30920/project-" + projectId + "/_doc";

        Map<String, Object> documnet = Map.of(
                "filename", file.filename(),
                "content", file.content(),
                "timestamp", file.timestamp().toString()
        );

        restTemplate.postForEntity(indexUrl, documnet, String.class);
    }

    private void saveFile(Long projectId, ExtractedFile file) {
        try {
            saveToElasticsearch(projectId, file);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        // Elasticsearch에 저장 성공 시에만 로컬 db도 저장
        saveToDatabase(projectId, file);
    }
}

