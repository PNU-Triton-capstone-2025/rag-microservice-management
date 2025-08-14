package com.triton.msa.triton_dashboard.private_data.util;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadedFileResultDto;
import com.triton.msa.triton_dashboard.private_data.exception.ZipSlipException;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ZipExtractor {

    private final Tika tika = new Tika();

    public List<ExtractedFile> extract(MultipartFile file, List<UploadedFileResultDto> skipped) throws IOException {
        Path tempDir = Files.createTempDirectory("upload-zip");

        try (InputStream inputStream = file.getInputStream();
             ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, StandardCharsets.UTF_8.name(), true)) {

            List<ExtractedFile> files = new ArrayList<>();
            ZipArchiveEntry entry;

            while ((entry = zis.getNextZipEntry()) != null) {
                if (entry.isDirectory()) continue;

                String filename = entry.getName();
                Path newFile = tempDir.resolve(filename).normalize();

                if (!newFile.startsWith(tempDir)) throw new ZipSlipException("압축 파일에 보안 취약성이 감지되어 업로드가 중단되었습니다.");

                Files.createDirectories(newFile.getParent());
                Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);

                try {
                    String content = extractContent(filename, newFile);
                    if (content == null || content.isBlank()) {
                        skipped.add(new UploadedFileResultDto(filename, "추출된 데이터 없음"));
                        continue;
                    }

                    files.add(new ExtractedFile(filename, content, Instant.now()));
                } catch (IOException | TikaException e) {
                    skipped.add(new UploadedFileResultDto(filename, "본문 추출에 실패했습니다"));
                }
            }

            return files;

        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    private String extractContent(String filename, Path file) throws IOException, TikaException {
        if (FileTypeUtil.isPlainText(filename)) {
            return Files.readAllLines(file, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
        }
        return tika.parseToString(file);
    }
}