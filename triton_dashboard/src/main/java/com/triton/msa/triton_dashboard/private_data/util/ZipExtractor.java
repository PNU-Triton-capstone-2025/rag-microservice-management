package com.triton.msa.triton_dashboard.private_data.util;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
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

    public List<ExtractedFile> extract(MultipartFile file, List<String> skipped) throws IOException {
        Path tempDir = Files.createTempDirectory("upload-zip");

        try (InputStream inputStream = file.getInputStream();
             ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, "CP949", true)) {

            List<ExtractedFile> files = new ArrayList<>();
            ZipArchiveEntry entry;

            while ((entry = zis.getNextZipEntry()) != null) {
                if (entry.isDirectory()) continue;

                String filename = entry.getName();
                Path newFile = tempDir.resolve(filename).normalize();

                if (!newFile.startsWith(tempDir)) throw new IOException("Zip Slip 공격 탐지됨");

                Files.createDirectories(newFile.getParent());
                Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);

                try {
                    String content = extractContent(filename, newFile);
                    if (content == null || content.isBlank()) {
                        skipped.add(filename + " (추출된 데이터 없음)");
                        continue;
                    }

                    files.add(new ExtractedFile(filename, content, Instant.now()));
                } catch (IOException | TikaException e) {
                    skipped.add(filename + " (텍스트 추출 실패: UTF-8 인코딩 오류)");
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