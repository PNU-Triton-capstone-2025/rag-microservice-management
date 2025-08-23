package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.monitoring.dto.MonitoringLogAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisEndpoint;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisEndpointService;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogAnalysisScheduler {

    private final LogMonitoringClient logMonitoringClient;
    private final LogAnalysisEndpointService logAnalysisEndpointService;
    private final LogAnalysisService logAnalysisService;
    private final ProjectService projectService;

    @Scheduled(fixedRate = 180000)
    public void analyzeErrorLogs() {
        log.info("starting scheduled log analysis...");

        List<Project> projects = projectService.getProjects();

        for(Project project : projects) {
            analyzeProjectErrorLogs(project.fetchId());
        }

        log.info("finished triggering log analysis for all projects.");
    }

    private void analyzeProjectErrorLogs(Long projectId) {
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

            LogAnalysisEndpoint endpoint = logAnalysisEndpointService.getEndpoint(projectId);

            MonitoringLogAnalysisRequestDto requestDto = new MonitoringLogAnalysisRequestDto(
                    endpoint.fetchProvider().toString(),
                    endpoint.fetchModel().toString(),
                    prompt
            );

            logAnalysisService.processLogAnalysisAsync(projectId, requestDto);
        }
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
