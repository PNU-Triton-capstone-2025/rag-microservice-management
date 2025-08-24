package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.monitoring.client.RagLogClient;
import com.triton.msa.triton_dashboard.monitoring.dto.MonitoringAnalysisResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.MonitoringLogAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisEndpoint;
import com.triton.msa.triton_dashboard.monitoring.client.ElasticSearchLogClient;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisEndpointService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisManager {
    private final MonitoringHistoryService monitoringHistoryService;
    private final ElasticSearchLogClient logMonitoringClient;
    private final LogAnalysisEndpointService endpointService;
    private final RagLogClient logAnalysisClient;

    @Async("logAnalysisTaskExecutor")
    public void analyzeProjectErrorLogs(Long projectId) {
        List<String> services = logMonitoringClient.getServices(projectId);

        if(services.isEmpty()) {
            return;
        }

        List<Map<String, String>> projectErrorLogs = new ArrayList<>();
        for(String service : services) {
            List<String> errorLogList = logMonitoringClient.getRecentErrorLogs(projectId, service, 3);
            if(!errorLogList.isEmpty()) {
                Map<String, String> serviceLogs = new HashMap<>();
                String errorLogs = String.join("\n", errorLogList);
                serviceLogs.put("serviceName", service);
                serviceLogs.put("logs", errorLogs);

                projectErrorLogs.add(serviceLogs);
            }
        }
        if(!projectErrorLogs.isEmpty()) {
            String prompt = buildPrompt(projectErrorLogs);

            LogAnalysisEndpoint endpoint = endpointService.getEndpoint(projectId);

            MonitoringLogAnalysisRequestDto requestDto = new MonitoringLogAnalysisRequestDto(
                    "project-" + projectId,
                    endpoint.fetchProvider().toString(),
                    endpoint.fetchModel().toString(),
                    prompt
            );

            logAnalysisClient.analyzeLogs(projectId, requestDto)
                    .flatMap(response -> {
                        if (response == null) {
                            log.warn("Received null response, skipping save for project ID: {}", projectId);
                        }
                        return saveHistoryAsync(projectId, response);
                    })
                    .subscribe(
                            null,
                            error -> log.error("[LogAnalysis] Failed to analyze error logs for project ID: {}", projectId, error),
                            () -> log.info("[LogAnalysis] Successfully analyzed error logs for project ID: {}", projectId)
                    );
        }
    }

    private Mono<Void> saveHistoryAsync(Long projectId, MonitoringAnalysisResponseDto responseDto) {
        return Mono.fromRunnable(() -> monitoringHistoryService.saveHistory(projectId, responseDto))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private String buildPrompt(List<Map<String, String>> errorLogs) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 컨테이너 기반 마이크로서비스로 구성된 각 서비스의 에러 로그들입니다.");
        for(Map<String, String> serviceErrorLogs : errorLogs) {
            String serviceName = serviceErrorLogs.get("serviceName");
            String logs = serviceErrorLogs.get("logs");
            sb.append("### 서비스 '").append(serviceName).append("'의 최근 3분간 에러 로그 ###\n");
            sb.append("```\n").append(logs).append("\n```\n\n");
        }
        sb.append("\n이 로그들의 핵심 원인은 무엇이며, 어떤 해결 방안을 제안할 수 있나요?");

        return sb.toString();
    }
}
