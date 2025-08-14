package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadedFileResultDto;
import com.triton.msa.triton_dashboard.private_data.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"}) // WebClient Mocking을 위한 경고 무시
@ExtendWith(MockitoExtension.class)
class PrivateDataPersistenceServiceTest {

    @InjectMocks
    private PrivateDataPersistenceService privateDataPersistenceService;

    @Mock
    private PrivateDataRepository privateDataRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    @DisplayName("파일 저장 성공 시 true를 반환")
    void saveFile_Success() {
        // given
        Long projectId = 1L;
        ExtractedFile file = new ExtractedFile("test.txt", "content", Instant.now());
        List<UploadedFileResultDto> skipped = new ArrayList<>();

        when(privateDataRepository.existsByProjectIdAndFilename(projectId, file.filename())).thenReturn(false);

        // WebClient Mocking 설정: webClient.post().uri(...).body(...).retrieve().bodyToMono() 호출 시 성공(빈 Mono)을 반환하도록 설정
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        when(projectService.getProject(projectId)).thenReturn(mock(Project.class));

        // when
        boolean result = privateDataPersistenceService.saveFile(projectId, file, skipped);

        // then
        assertThat(result).isTrue();
        assertThat(skipped).isEmpty();
        verify(privateDataRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("DB 저장 실패 시 ES에 저장된 데이터를 삭제 시도 후 false를 반환")
    void saveFile_Fail_Rollback() {
        // given
        Long projectId = 1L;
        ExtractedFile file = new ExtractedFile("test.txt", "content", Instant.now());
        List<UploadedFileResultDto> skipped = new ArrayList<>();

        when(privateDataRepository.existsByProjectIdAndFilename(projectId, file.filename())).thenReturn(false);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        // ES 저장과 삭제 모두 bodyToMono/toBodilessEntity를 사용하므로 한번에 설정
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.empty());
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        when(projectService.getProject(projectId)).thenReturn(mock(Project.class));

        doThrow(new RuntimeException("DB save Error")).when(privateDataRepository).save(any());

        // when
        boolean result = privateDataPersistenceService.saveFile(projectId, file, skipped);

        // then
        assertThat(result).isFalse();
        assertThat(skipped).hasSize(1);
        verify(privateDataRepository, times(1)).save(any());
        assertThat(skipped.get(0).reason()).isEqualTo("시스템 오류로 저장되지 않았습니다.");

        // ES 저장 -> DB 저장 실패 -> ES 삭제. ES 요청이 총 2번 이뤄졌는지
        verify(webClient, times(2)).post();
    }
}