package com.triton.msa.triton_dashboard.private_data.util;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadedFileResultDto;
import com.triton.msa.triton_dashboard.private_data.exception.ZipSlipException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZipExtractorTest {

    private ZipExtractor zipExtractor;

    @BeforeEach
    void setUp() {
        zipExtractor = new ZipExtractor();
    }

    @Test
    @DisplayName("정상적인 ZIP 파일에서 테스트 파일들을 성공적으로 추출한다")
    void extractZip() throws IOException {
        // given
        byte[] zipBytes = createZipBytes(
                new String[]{"test1.txt", "docs/test2.md"},
                new String[]{"hello", "## markdown"}
        );
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.zip", "application/zip", zipBytes);
        List<UploadedFileResultDto> skipped = new ArrayList<>();

        // when
        List<ExtractedFile> result = zipExtractor.extract(multipartFile, skipped);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).filename()).isEqualTo("test1.txt");
        assertThat(result.get(0).content()).isEqualTo("hello");
        assertThat(result.get(1).filename()).isEqualTo("docs/test2.md");
        assertThat(result.get(1).content()).isEqualTo("## markdown");
        assertThat(skipped).isEmpty();
    }

    @Test
    @DisplayName("ZIP 파일에 내용이 없으면 skipped에 추가됨")
    void extractZipSkipped() throws IOException {
        // given
        byte[] zipBytes = createZipBytes(
                new String[]{"empty.txt", "valid.txt"},
                new String[]{"", "content"}
        );
        MockMultipartFile multipartFile = new MockMultipartFile("file", "text.zip", "application/zip", zipBytes);
        List<UploadedFileResultDto> skipped = new ArrayList<>();

        // when
        List<ExtractedFile> result = zipExtractor.extract(multipartFile, skipped);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).filename()).isEqualTo("valid.txt");

        assertThat(skipped).hasSize(1);
        assertThat(skipped.get(0).filename()).isEqualTo("empty.txt");
        assertThat(skipped.get(0).reason()).isEqualTo("추출된 데이터 없음");
    }

    @Test
    @DisplayName("Zip Slip 공격 시도가 감지되면 ZipSlipException을 던진다")
    void extractZipSlip() throws IOException {
        // given
        byte[] zipBytes = createZipBytes(
            new String[]{"../../evil.txt"},
            new String[]{"hacked"}
        );
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.zip", "application/zip", zipBytes);
        List<UploadedFileResultDto> skipped = new ArrayList<>();

        // when & then
        assertThrows(ZipSlipException.class, () ->
            zipExtractor.extract(multipartFile, skipped)
        );
    }

    // 테스트용 zip 파일 만드는 메서드
    private byte[] createZipBytes(String[] fileNames, String[] contents) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < fileNames.length; i++) {
                ZipEntry entry = new ZipEntry(fileNames[i]);
                zos.putNextEntry(entry);
                zos.write(contents[i].getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }
}