package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogAnalysisScheduler {

    private final LogMonitoringClient logMonitoringClient;
    private final RagHistoryService ragHistoryService;
    private final ProjectService projectService;
    private final WebClient webClient;

    @Scheduled(fixedRate = 180000)
    public void analyzeErrorLogs() {
        log.info("starting scheduled log analysis...");

        List<Project> projects = projectService.getProjects();

        for(Project project : projects) {
            analyzeProjectErrorLogs(project.fetchId());
        }
    }

    private void analyzeProjectErrorLogs(Long projectId) {
        List<String> services = logMonitoringClient.getServices(projectId);

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
        }
    }

    private String buildPrompt(List<Map<String, String>> errorLogs) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 컨테이너 기반 마이크로서비스로 구성된 각 서비스의 에러 로그들입니다.");
        for(Map<String, String> serviceErrorLogs : errorLogs) {
            String serviceName = serviceErrorLogs.get("serviceName");
            String logs = serviceErrorLogs.get("logs");
            sb.append("서비스 '").append(serviceName).append("'에서 최근 3분 동안 다음 에러 로그들이 발생했습니다:\n");
            sb.append(logs);
        }
        sb.append("\n이 로그들의 핵심 원인은 무엇이며, 어떤 해결 방안을 제안할 수 있나요?");

        return sb.toString();
    }
}
