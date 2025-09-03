package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.monitoring.client.RagLogClient;
import com.triton.msa.triton_dashboard.monitoring.dto.ErrorAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceMetricDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.monitoring.client.ElasticSearchLogClient;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisModelService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringHistoryService;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringService;
import com.triton.msa.triton_dashboard.monitoring.util.ResourceAdvisor;
import com.triton.msa.triton_dashboard.project.entity.SavedYaml;
import com.triton.msa.triton_dashboard.user.dto.UserApiKeyRequestDto;
import com.triton.msa.triton_dashboard.user.entity.User;
import com.triton.msa.triton_dashboard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final MonitoringService monitoringService;
    private final UserService userService;

    private static final int ANALYSIS_PERIOD_MINUTES = 3;

    @Async("logAnalysisTaskExecutor")
    public void analyzeProjectLogs(Long projectId) {
        List<String> services = logMonitoringClient.getServices(projectId);
        if(services.isEmpty()) {
            return;
        }
        log.info("project-{} service 개수 : {}", projectId, services.size());
        for(int i = 0; i < services.size(); i++) {
            log.info("service : {}", services.get(i));
        }

        List<Map<String, String>> projectErrorLogs = fetchProjectErrorLogs(projectId, services);
        log.info("project-{} 에러 로그 개수 : {}", projectId, projectErrorLogs.size());
        String serviceResources = fetchResources(projectId, services);
        log.info("project-{} 리소스 정보 : {}", projectId, serviceResources);
        List<SavedYaml> savedYamls = monitoringService.getSavedYamlsWithContent(projectId);

        boolean hasErrorLogs = !projectErrorLogs.isEmpty();
        boolean hasResourceInfo = serviceResources != null && !serviceResources.isBlank();
        boolean hasYamls = !savedYamls.isEmpty();

        if (!((hasErrorLogs || hasResourceInfo) && hasYamls)) {
            log.info("[LogAnalysis] Not enough data to analyze for project ID: {}. Skipping. (Errors: {}, Resources: {}, YAMLs: {})",
                    projectId, hasErrorLogs, hasResourceInfo, hasYamls);
            return;
        }

        LogAnalysisModel model = endpointService.getAnalysisModel(projectId);
        if(model == null) {
            log.info("[LogAnalysis] AI model not configured for project ID: {}", projectId);
            return;
        }

        User user = userService.getUserByProjectId(projectId);
        String apiKey = userService.getCurrentUserApiKey(user.getUsername(), new UserApiKeyRequestDto(model.fetchProvider()));
        String yamlsAsString = savedYamls.stream()
                .map(y -> "--- \n# Filename: " + y.getFileName() + "\n" + y.getYamlContent())
                .collect(Collectors.joining("\n"));
        
        if(!projectErrorLogs.isEmpty()) {
            analyzeProjectErrorLogs(projectId, model, apiKey, projectErrorLogs, yamlsAsString);
        }

        if (serviceResources != null && !serviceResources.isBlank()) {
            analyzeResourceUsage(projectId, model, apiKey, serviceResources, yamlsAsString);
        }
    }

    private void analyzeProjectErrorLogs(Long projectId, LogAnalysisModel model, String apiKey, List<Map<String, String>> projectErrorLogs, String yamlsAsString) {
        String errorLogsAsString = projectErrorLogs.stream()
                .map(logMap -> String.format("### Service: %s\n```\n%s\n```", logMap.get("serviceName"), logMap.get("logs")))
                .collect(Collectors.joining("\n\n"));

        ErrorAnalysisRequestDto requestDto = new ErrorAnalysisRequestDto(
                "project-" + projectId,
                model.fetchProvider().toValue(),
                apiKey,
                errorLogsAsString,
                yamlsAsString
        );

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String title = "에러 로그 해결 방안 - " + now;

        log.info("[LogAnalysis] Sending error log analysis request for project ID: {}", projectId);
        logAnalysisClient.analyzeErrorLogs(requestDto)
                .flatMap(response -> {
                    if (response == null) {
                        log.warn("Received null response, skipping save for project ID: {}", projectId);
                        return Mono.empty();
                    }
                    return saveHistoryAsync(projectId, response, title);
                })
                .subscribe(
                null,
                        error -> log.error("[LogAnalysis] Error processing error log analysis for project ID: {}", projectId, error),
                        () -> log.info("[LogAnalysis] Successfully completed error log analysis for project ID: {}", projectId)
                );
    }

    private void analyzeResourceUsage(Long projectId, LogAnalysisModel model, String apiKey, String serviceResources, String yamlsAsString) {
        ResourceAnalysisRequestDto requestDto = new ResourceAnalysisRequestDto(
                "project-" + projectId,
                model.fetchProvider().toValue(),
                model.fetchModel().getModelName(),
                apiKey,
                serviceResources,
                yamlsAsString
        );

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String title = "리소스 분석 - " + now;

        log.info("[LogAnalysis] Sending resource usage analysis request for project ID: {}", projectId);
        logAnalysisClient.analyzeResourceSettings(requestDto)
                .flatMap(response -> {
                    if (response == null) {
                        log.warn("Received null response, skipping save for project ID: {}", projectId);
                        return Mono.empty();
                    }
                    return saveHistoryAsync(projectId, response, title);
                })
                .subscribe(
                        null,
                        error -> log.error("[LogAnalysis] Error processing error log analysis for project ID: {}", projectId, error),
                        () -> log.info("[LogAnalysis] Successfully completed error log analysis for project ID: {}", projectId)
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

    private String fetchResources(Long projectId, List<String> services) {
        return services.stream()
                .map(service -> {
                    ResourceMetricDto metricDto = logMonitoringClient.getServiceResourceMetrics(projectId, service, ANALYSIS_PERIOD_MINUTES);
                    log.info("project-{}-metrics-* 메트릭 분석", projectId);
                    if (metricDto.maxCpu() == 0 && metricDto.maxMemoryBytes() == 0) {
                        return null;
                    }

                    return resourceAdvisor.generatePerformancePrompt(service, metricDto);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));
    }

    private Mono<Void> saveHistoryAsync(Long projectId, RagLogResponseDto responseDto, String defaultTitle) {
        String title = (responseDto.title() != null && !responseDto.title().isBlank()) ? responseDto.title() : defaultTitle;
        RagLogResponseDto finalResponse = new RagLogResponseDto(title, responseDto.answer());

        return Mono.fromRunnable(() -> monitoringHistoryService.saveHistory(projectId, finalResponse))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
