package com.triton.msa.triton_dashboard.monitoring.scheduler;

import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogAnalysisScheduler {

    private final LogAnalysisManager logAnalysisService;
    private final ProjectService projectService;

    @Scheduled(fixedRateString = "${monitoring.scheduler.rate}")
    public void analyzeErrorLogs() {
        log.info("starting scheduled log analysis...");

        List<Project> projects = projectService.getProjects();

        for(Project project : projects) {
            logAnalysisService.analyzeProjectLogs(project.fetchId());
        }

        log.info("finished triggering log analysis for all projects.");
    }
}
