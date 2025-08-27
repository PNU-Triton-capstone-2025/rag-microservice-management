package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.monitoring.client.RagLogClient;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RecommendedResourcesDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceMetricDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.monitoring.client.ElasticSearchLogClient;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisModelService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringHistoryService;
import com.triton.msa.triton_dashboard.monitoring.util.LogAnalysisPromptBuilder;
import com.triton.msa.triton_dashboard.monitoring.util.ResourceAdvisor;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisManager {
    private final MonitoringHistoryService monitoringHistoryService;
    private final ElasticSearchLogClient logMonitoringClient;
    private final LogAnalysisModelService endpointService;
    private final RagLogClient logAnalysisClient;
    private final ResourceAdvisor resourceAdvisor;

    private static final int ANALYSIS_PERIOD_MINUTES = 3;

    @Async("logAnalysisTaskExecutor")
    public void analyzeProjectErrorLogs(Long projectId) {
        List<String> services = logMonitoringClient.getServices(projectId);
        if(services.isEmpty()) {
            return;
        }

        List<Map<String, String>> projectErrorLogs = fetchProjectErrorLogs(projectId, services);
        String resourceSuggestion = fetchResourceSuggestions(projectId, services);

        if (projectErrorLogs.isEmpty() && resourceSuggestion.isEmpty()) {
            return;
        }
        RagLogRequestDto requestDto = makeLogAnalysisTemplate(projectId, projectErrorLogs, resourceSuggestion);

        logAnalysisClient.analyzeLogs(projectId, requestDto)
                .flatMap(response -> {
                    if (response == null) {
                        log.warn("Received null response, skipping save for project ID: {}", projectId);
                        return Mono.empty();
                    }
                    return saveHistoryAsync(projectId, response);
                })
                .subscribe(
                        null,
                        error -> log.error("[LogAnalysis] Failed to analyze error logs for project ID: {}", projectId, error),
                        () -> log.info("[LogAnalysis] Successfully analyzed error logs for project ID: {}", projectId)
                );
    }

    private RagLogRequestDto makeLogAnalysisTemplate(Long projectId, List<Map<String, String>> projectErrorLogs, String resourceSuggestion) {
        String prompt = LogAnalysisPromptBuilder.buildPrompt(projectErrorLogs, resourceSuggestion);

        LogAnalysisModel model = endpointService.getAnalysisModel(projectId);

        return new RagLogRequestDto(
                "project-" + projectId,
                model.fetchProvider().toString(),
                model.fetchModel().toString(),
                prompt
        );
    }

    private List<Map<String, String>> fetchProjectErrorLogs(Long projectId, List<String> services) {
        List<Map<String, String>> projectErrorLogs = new ArrayList<>();
        for(String service : services) {
            fetchServiceErrorLogs(projectId, service, projectErrorLogs);
        }
        return projectErrorLogs;
    }

    private void fetchServiceErrorLogs(Long projectId, String service, List<Map<String, String>> projectErrorLogs) {
        List<String> errorLogList = logMonitoringClient.getRecentErrorLogs(projectId, service, ANALYSIS_PERIOD_MINUTES);
        if(!errorLogList.isEmpty()) {
            Map<String, String> serviceLogs = new HashMap<>();
            String errorLogs = String.join("\n", errorLogList);
            serviceLogs.put("serviceName", service);
            serviceLogs.put("logs", errorLogs);

            projectErrorLogs.add(serviceLogs);
        }
    }

    private String fetchResourceSuggestions(Long projectId, List<String> services) {
        return services.stream()
                .map(service -> {
                    ResourceMetricDto metricDto = logMonitoringClient.getServiceResourceMetrics(projectId, service, ANALYSIS_PERIOD_MINUTES);
                    if (metricDto.maxCpu() == 0 && metricDto.maxMemoryBytes() == 0) {
                        return null;
                    }

                    RecommendedResourcesDto recommended = resourceAdvisor.recommendResources(metricDto);

                    return resourceAdvisor.generatePerformancePrompt(service, recommended);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));
    }

    private Mono<Void> saveHistoryAsync(Long projectId, RagLogResponseDto responseDto) {
        return Mono.fromRunnable(() -> monitoringHistoryService.saveHistory(projectId, responseDto))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
