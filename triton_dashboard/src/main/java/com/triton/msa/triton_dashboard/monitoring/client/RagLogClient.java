package com.triton.msa.triton_dashboard.monitoring.client;

import com.triton.msa.triton_dashboard.monitoring.dto.ErrorAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceAnalysisRequestDto;
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

    @Value("${rag.service.url}")
    private String ragServerBaseUrl;

    public Mono<RagLogResponseDto> analyzeErrorLogs(ErrorAnalysisRequestDto requestDto) {
        return webClient.post()
                .uri(ragServerBaseUrl + "/api/log-analyze")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(RagLogResponseDto.class)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("[Async] Failed to analyze error logs. Status: {}, Body: {}",
                                ex.getStatusCode(), ex.getResponseBodyAsString()))
                .doOnError(ex -> log.error("[Async] An unexpected error occurred during error log analysis.", ex))
                .onErrorResume(ex -> Mono.empty());
    }

    public Mono<RagLogResponseDto> analyzeResourceSettings(ResourceAnalysisRequestDto requestDto) {
        return webClient.post()
                .uri(ragServerBaseUrl + "/api/resource-setting")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(RagLogResponseDto.class)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("[Async] Failed to analyze resource settings. Status: {}, Body: {}",
                                ex.getStatusCode(), ex.getResponseBodyAsString()))
                .doOnError(ex -> log.error("[Async] An unexpected error occurred during resource setting analysis.", ex))
                .onErrorResume(ex -> Mono.empty());
    }
}
