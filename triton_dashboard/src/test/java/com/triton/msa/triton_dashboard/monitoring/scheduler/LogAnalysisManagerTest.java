package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.monitoring.client.ElasticSearchLogClient;
import com.triton.msa.triton_dashboard.monitoring.client.RagLogClient;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisModelService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringHistoryService;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogAnalysisManagerTest {
    @InjectMocks
    private LogAnalysisManager logAnalysisManager;
    @Mock
    private MonitoringHistoryService monitoringHistoryService;
    @Mock
    private ElasticSearchLogClient logClient;
    @Mock
    private LogAnalysisModelService modelService;
    @Mock
    private RagLogClient ragLogClient;

    @Test
    @DisplayName("프로젝트 에러 로그 분석 및 저장 전체 흐름 - 200")
    void analyzeProjectErrorLogs() {
        // given
        Long projectId = 1L;
        List<String> services = List.of("service-A", "service-B");
        List<String> serviceALogs = List.of("Error in A-1", "Error in A-2");
        List<String> serviceBLogs = List.of("Error in B-1");

        LogAnalysisModel model = new LogAnalysisModel(LlmProvider.OPENAI, LlmModel.GPT_4O);
        RagLogResponseDto analysisResponse = new RagLogResponseDto("Analysis Title", "Analysis Report");

        when(logClient.getServices(projectId)).thenReturn(services);
        when(logClient.getRecentErrorLogs(projectId, "service-A", 3)).thenReturn(serviceALogs);
        when(logClient.getRecentErrorLogs(projectId, "service-B", 3)).thenReturn(serviceBLogs);
        when(modelService.getAnalysisModel(projectId)).thenReturn(model);
        when(ragLogClient.analyzeLogs(eq(projectId), any(RagLogRequestDto.class))).thenReturn(Mono.just(analysisResponse));
        doNothing().when(monitoringHistoryService).saveHistory(eq(projectId), any(RagLogResponseDto.class));
        // when
        logAnalysisManager.analyzeProjectErrorLogs(projectId);

        // then
        // @Async
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(logClient).getServices(projectId);
            verify(logClient).getRecentErrorLogs(projectId, "service-A", 3);
            verify(logClient).getRecentErrorLogs(projectId, "service-B", 3);
            verify(modelService).getAnalysisModel(projectId);
            verify(ragLogClient).analyzeLogs(eq(projectId), any(RagLogRequestDto.class));
            verify(monitoringHistoryService).saveHistory(eq(projectId), any(RagLogResponseDto.class));
        });
    }

    @Test
    @DisplayName("에러 로그가 없는 경우 RAG 서버 호출 및 저장 로직이 실행되지 않음")
    void analyzeProjectErrorLogsInNoErrors(){
        // given
        Long projectId = 2L;
        List<String> services = List.of("service-C");

        when(logClient.getServices(projectId)).thenReturn(services);
        when(logClient.getRecentErrorLogs(projectId, "service-C", 3)).thenReturn(Collections.emptyList());

        // when
        logAnalysisManager.analyzeProjectErrorLogs(projectId);

        // then
        // 비동기 호출이 없으므로 잠시 대기 후 검증
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(logClient).getServices(projectId);
        verify(logClient).getRecentErrorLogs(projectId, "service-C", 3);

        verify(modelService, never()).getAnalysisModel(anyLong());
        verify(ragLogClient, never()).analyzeLogs(anyLong(), any());
        verify(monitoringHistoryService, never()).saveHistory(anyLong(), any());
    }
}
