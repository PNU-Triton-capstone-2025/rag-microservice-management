package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataUploadResultDto;
import com.triton.msa.triton_dashboard.private_data.exception.UnsupportedFileTypeException;
import com.triton.msa.triton_dashboard.private_data.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.private_data.util.ZipExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateDataServiceTest {

    @InjectMocks
    private PrivateDataService privateDataService;

    @Mock
    private ZipExtractor zipExtractor;

    @Mock
    private PrivateDataPersistenceService privateDataPersistenceService;

    @Mock
    private PrivateDataRepository privateDataRepository;

    @Test
    @DisplayName("정상적인 ZIP 파일 업로드 시 성공적으로 처리 결과를 반환한다")
    void unzipAndSaveFiles_Success() {
        // given
        Long projectId = 1L;
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.zip", "application/zip", "content".getBytes());

        ExtractedFile validFile = new ExtractedFile("config.yml", "key: value", Instant.now());
        ExtractedFile forbiddenFile = new ExtractedFile("hack.sh", "echo hello", Instant.now());
        List<ExtractedFile> extractedFiles = List.of(validFile,forbiddenFile);

        when(zipExtractor.extract(eq(multipartFile), any(List.class))).thenReturn(extractedFiles);
        when(privateDataPersistenceService.saveFile(eq(projectId), eq(validFile), any(List.class))).thenReturn(true);

        // when
        PrivateDataUploadResultDto result = privateDataService.unzipAndSaveFiles(projectId, multipartFile);

        // then
        assertThat(result.message()).isEqualTo("업로드 완료 (성공 1건, 스킵 1건)");
        assertThat(result.savedFilenames()).hasSize(1);
        assertThat(result.savedFilenames().get(0).filename()).isEqualTo(validFile.filename());

        assertThat(result.skippedFilenames()).hasSize(1);
        assertThat(result.skippedFilenames().get(0).filename()).isEqualTo(forbiddenFile.filename());
        assertThat(result.skippedFilenames().get(0).reason()).isEqualTo("허용되지 않음");
    }

    @Test
    @DisplayName("ZIP 파일이 아닌 경우 UnsupportedFileTypeException을 던진다")
    void unzipAndSaveFiles_ThrowsException_WhenNotZip() {
        // given
        Long projectId = 1L;
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        // when & then
        assertThrows(UnsupportedFileTypeException.class, () -> {
            privateDataService.unzipAndSaveFiles(projectId, multipartFile);
        });
    }
}