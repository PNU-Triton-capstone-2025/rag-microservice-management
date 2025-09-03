package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.monitoring.client.ElasticSearchLogClient;
import com.triton.msa.triton_dashboard.monitoring.client.RagLogClient;
import com.triton.msa.triton_dashboard.monitoring.dto.ErrorAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceMetricDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisModelService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringHistoryService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringService;
import com.triton.msa.triton_dashboard.monitoring.util.ResourceAdvisor;
import com.triton.msa.triton_dashboard.project.entity.SavedYaml;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.service.UserService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogAnalysisManagerTest {
    @InjectMocks
    private LogAnalysisManager logAnalysisManager;
    @Mock
    private MonitoringHistoryService monitoringHistoryService;
    @Mock
    private ElasticSearchLogClient logMonitoringClient;
    @Mock
    private LogAnalysisModelService modelService;
    @Mock
    private RagLogClient ragLogClient;
    @Mock
    private ResourceAdvisor resourceAdvisor;
    @Mock
    private MonitoringService monitoringService;
    @Mock
    private UserService userService;

    @Test
    @DisplayName("프로젝트 에러 로그 및 리소스 분석 및 저장 전체 흐름 - 200")
    void analyzeProjectLogs_Success() {
        // given
        Long projectId = 1L;
        String username = "testuser";
        List<String> services = List.of("service-A");
        List<String> serviceALogs = List.of("Error in A-1");
        List<SavedYaml> savedYamls = List.of(new SavedYaml("test.yml", "content"));
        User mockUser = mock(User.class);

        LogAnalysisModel model = new LogAnalysisModel(LlmProvider.OPENAI, LlmModel.GPT_4O);
        RagLogResponseDto errorAnalysisResponse = new RagLogResponseDto("Error Analysis", "Error Report");
        RagLogResponseDto resourceAnalysisResponse = new RagLogResponseDto("Resource Analysis", "Resource Report");
        ResourceMetricDto dummyMetrics = new ResourceMetricDto(0.1, 0.05, 0.2, 100000, 50000, 200000);
        String performancePrompt = "Performance suggestion for service-A";

        when(logMonitoringClient.getServices(projectId)).thenReturn(services);
        when(logMonitoringClient.getRecentErrorLogs(projectId, "service-A", 3)).thenReturn(serviceALogs);
        when(logMonitoringClient.getServiceResourceMetrics(projectId, "service-A", 3)).thenReturn(dummyMetrics);
        when(monitoringService.getSavedYamlsWithContent(projectId)).thenReturn(savedYamls);
        when(resourceAdvisor.generatePerformancePrompt(anyString(), any(ResourceMetricDto.class))).thenReturn(performancePrompt);
        when(modelService.getAnalysisModel(projectId)).thenReturn(model);
        when(userService.getUserByProjectId(projectId)).thenReturn(mockUser);
        when(mockUser.getUsername()).thenReturn(username);
        when(userService.getCurrentUserApiKey(eq(username), any())).thenReturn("test-api-key");

        when(ragLogClient.analyzeErrorLogs(any(ErrorAnalysisRequestDto.class))).thenReturn(Mono.just(errorAnalysisResponse));
        when(ragLogClient.analyzeResourceSettings(any(ResourceAnalysisRequestDto.class))).thenReturn(Mono.just(resourceAnalysisResponse));

        doNothing().when(monitoringHistoryService).saveHistory(eq(projectId), any(RagLogResponseDto.class));

        // when
        logAnalysisManager.analyzeProjectLogs(projectId);

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(logMonitoringClient).getServices(projectId);
            verify(logMonitoringClient).getRecentErrorLogs(projectId, "service-A", 3);
            verify(logMonitoringClient).getServiceResourceMetrics(projectId, "service-A", 3);
            verify(ragLogClient).analyzeErrorLogs(any(ErrorAnalysisRequestDto.class));
            verify(ragLogClient).analyzeResourceSettings(any(ResourceAnalysisRequestDto.class));
            verify(monitoringHistoryService, times(2)).saveHistory(eq(projectId), any(RagLogResponseDto.class));
        });
    }


    @Test
    @DisplayName("에러 로그, 메트릭 정보가 없는 경우 RAG 서버 호출 및 저장 로직이 실행되지 않음")
    void analyzeProjectLogs_NoDataToAnalyze(){
        // given
        Long projectId = 2L;
        List<String> services = List.of("service-C");
        List<SavedYaml> savedYamls = List.of(new SavedYaml("test.yml", "content"));

        when(logMonitoringClient.getServices(projectId)).thenReturn(services);
        when(logMonitoringClient.getRecentErrorLogs(projectId, "service-C", 3)).thenReturn(Collections.emptyList());
        when(monitoringService.getSavedYamlsWithContent(projectId)).thenReturn(savedYamls);
        ResourceMetricDto emptyMetrics = new ResourceMetricDto(0, 0, 0, 0, 0, 0);
        when(logMonitoringClient.getServiceResourceMetrics(projectId, "service-C", 3)).thenReturn(emptyMetrics);

        // when
        logAnalysisManager.analyzeProjectLogs(projectId);

        // then
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(logMonitoringClient).getServices(projectId);
            verify(logMonitoringClient).getRecentErrorLogs(projectId, "service-C", 3);
            verify(modelService, never()).getAnalysisModel(anyLong());
            verify(ragLogClient, never()).analyzeErrorLogs(any());
            verify(ragLogClient, never()).analyzeResourceSettings(any());
            verify(monitoringHistoryService, never()).saveHistory(anyLong(), any());
        });
    }
}
