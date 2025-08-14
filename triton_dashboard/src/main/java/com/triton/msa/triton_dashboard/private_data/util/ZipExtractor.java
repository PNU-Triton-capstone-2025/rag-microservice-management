package com.triton.msa.triton_dashboard.private_data.util;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadedFileResultDto;
import com.triton.msa.triton_dashboard.private_data.exception.PrivateDataUnzipException;
import com.triton.msa.triton_dashboard.private_data.exception.ZipSlipException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZipExtractor {

    private final Tika tika = new Tika();

    public List<ExtractedFile> extract(MultipartFile file, List<UploadedFileResultDto> skipped) {
        Path tempDir = createTempDirectory();
        List<ExtractedFile> extractedFiles = new ArrayList<>();

        try {
            // 스트림을 열고 파일 처리 실행
            try (InputStream inputStream = file.getInputStream();
                 ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, StandardCharsets.UTF_8.name(), true)) {

                extractFilesFromStream(zis, tempDir, extractedFiles, skipped);
            }
        } catch (IOException e) {
            throw new PrivateDataUnzipException("ZIP 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            cleanupTempDirectory(tempDir);
        }
        return extractedFiles;
    }

    /**
     * 압축 해제를 위한 임시 디렉토리 생성
     */
    private Path createTempDirectory() {
        try {
            return Files.createTempDirectory("upload-zip-" + System.nanoTime());
        } catch (IOException e) {
            throw new PrivateDataUnzipException("압축 해제를 위한 임시 디렉토리 생성에 실패했습니다. " + e.getMessage());
        }
    }

    /**
     * Zip 스트림을 순회하며 각 엔트리를 처리
     */
    private void extractFilesFromStream(ZipArchiveInputStream zis,
                                        Path tempDir,
                                        List<ExtractedFile> extractedFiles,
                                        List<UploadedFileResultDto> skipped) throws IOException {
        ZipArchiveEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            handleEntryProcessing(entry, zis, tempDir, extractedFiles, skipped);
        }
    }

    /**
     * 단일 ZIP 엔트리 처리와 예외 처리 담당
     */
    private void handleEntryProcessing(ZipArchiveEntry entry,
                                       ZipArchiveInputStream zis,
                                       Path tempDir,
                                       List<ExtractedFile> extractedFiles,
                                       List<UploadedFileResultDto> skipped) {
        try {
            processEntry(entry, zis, tempDir)
                .ifPresentOrElse(
                    extractedFiles::add,
                    () -> skipped.add(new UploadedFileResultDto(entry.getName(), "추출된 데이터 없음"))
                );
        } catch (ZipSlipException e) {
            // ZipSlip은 보안 위협이므로 전체 작업을 중단
            throw e;
        } catch (Exception e) {
            skipped.add(new UploadedFileResultDto(entry.getName(), "처리 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 실제 파일 추출 및 내용 변환 로직을 수행
     */
    private Optional<ExtractedFile> processEntry(ZipArchiveEntry entry, ZipArchiveInputStream zis, Path tempDir) throws IOException, TikaException {
        String filename = entry.getName();
        Path newFile = secureResolve(tempDir, filename);

        Files.createDirectories(newFile.getParent());
        Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);

        String content = extractContent(filename, newFile);

        if (content == null || content.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new ExtractedFile(filename, content, Instant.now()));
    }

    private Path secureResolve(Path baseDir, String filename) {
        Path resolvedPath = baseDir.resolve(filename).normalize();
        if (!resolvedPath.startsWith(baseDir)) {
            throw new ZipSlipException("압축 파일에 보안 취약성이 감지되었습니다: " + filename);
        }
        return resolvedPath;
    }

    /**
     * 파일의 내용을 추출
     */
    private String extractContent(String filename, Path file) throws IOException, TikaException {
        if (FileTypeUtil.isPlainText(filename)) {
            return String.join("\n", Files.readAllLines(file, StandardCharsets.UTF_8));
        }
        return tika.parseToString(file);
    }

    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir != null) {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                log.error("임시 디렉토리 삭제 실패: {}", tempDir, e);
            }
        }
    }
}