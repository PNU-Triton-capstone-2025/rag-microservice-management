package com.triton.msa.triton_dashboard.monitoring.client;

import com.triton.msa.triton_dashboard.monitoring.dto.MonitoringAnalysisResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagLogClient {
    private final WebClient webClient;

    @Value("${rag.server.url.monitoring}")
    private String ragServerUrl;

    public Mono<MonitoringAnalysisResponseDto> analyzeLogs(Long projectId, RagLogRequestDto requestDto) {
        return webClient.post()
                .uri(ragServerUrl)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(MonitoringAnalysisResponseDto.class)
                .doOnError(WebClientResponseException.class, ex ->
                    log.error("[Async] Failed to analyze logs. Status: {}, Body: {}",
                            ex.getStatusCode(), ex.getResponseBodyAsByteArray()))
                .doOnError(ex -> log.error("[Async] An unexpected error occurred during log analysis.",  ex))
                .onErrorResume(ex -> Mono.empty());
    }
}
