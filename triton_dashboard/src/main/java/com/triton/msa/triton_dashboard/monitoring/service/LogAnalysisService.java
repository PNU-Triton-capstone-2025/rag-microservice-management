package com.triton.msa.triton_dashboard.monitoring.service;

import com.triton.msa.triton_dashboard.monitoring.dto.MonitoringAnalysisResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.MonitoringLogAnalysisRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisService {
    private final WebClient webClient;
    private final MonitoringHistoryService monitoringHistoryService;
    @Value("${rag.server.url.monitoring}")
    private String ragServerUrl;

    @Async
    public void processLogAnalysisAsync(Long projectId, MonitoringLogAnalysisRequestDto requestDto) {
        webClient.post()
                .uri(ragServerUrl)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(MonitoringAnalysisResponseDto.class)
                .doOnSuccess(response -> {
                    if(response != null){
                        monitoringHistoryService.saveHistory(projectId, response);
                        log.info("[Async] Successfully saved analysis result for project ID: {}", projectId);
                    }
                    else{
                        log.warn("[Async] Received null response from RAG server for project ID: {}", projectId);
                    }
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("[Async] Failed to analyze logs for project ID: {}. Status: {}, Body: {}",
                            projectId, ex.getStatusCode(), ex.getResponseBodyAsByteArray());
                    return Mono.empty();
                })
                .onErrorResume(ex -> {
                    log.error("[Async] An unexpected error occurred during log analysis for project ID: {}", projectId, ex);
                    return Mono.empty();
                })
                .subscribe();
    }
}
