package com.triton.msa.triton_dashboard.user.util;

import com.triton.msa.triton_dashboard.user.dto.ApiKeyValidationResponseDto;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.exception.ApiKeysValidationException;
import com.triton.msa.triton_dashboard.user.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmApiKeyValidator {

    private final WebClient webClient;

    public void validateAll(UserRegistrationDto dto) {
        Map<String, Object> results = new LinkedHashMap<>();

       for (LlmProvider p : LlmProvider.values()) {
           String apiKey = dto.apiKeyOf(p);
           validateOne(p, apiKey, results);
       }

        if (results.values().stream().anyMatch(result -> result instanceof Exception)) {
            throw new ApiKeysValidationException(new ApiKeyValidationResponseDto(results), dto);
        }
    }

    // 내부 호출용. 비어있으면 스킵, 아니면 provider 별 ping 수행
    private void validateOne(LlmProvider provider, String apiKey, Map<String, Object> results) {
        if (apiKey == null || apiKey.isBlank()) {
            results.put(provider.name(), "skipped");
            return;
        }
        try {
            switch (provider) {
                case OPENAI -> pingOpenAI(apiKey).block();
                case CLAUDE -> pingAnthropic(apiKey).block();
                case GEMINI -> pingGoogle(apiKey).block();
            }
            results.put(provider.name(), "valid");
        } catch (Exception e) {
            results.put(provider.name(), e);
        }
    }

    // 외부 호출용. (API 키 변경 등)
    public void validateOne(LlmProvider provider, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }

        Mono<Void> validationMono = switch (provider) {
            case OPENAI -> pingOpenAI(apiKey);
            case CLAUDE -> pingAnthropic(apiKey);
            case GEMINI -> pingGoogle(apiKey);
        };

        try {
            validationMono.block();
        } catch (Exception e) {
            throw new InvalidApiKeyException("API 키 검증에 실패했습니다: " + e.getMessage());
        }
    }

    private Mono<Void> pingOpenAI(String apiKey) {
        return webClient.get()
                .uri("https://api.openai.com/v1/models")
                .headers(h -> h.setBearerAuth(apiKey))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(8));
    }

    private Mono<Void> pingAnthropic(String apiKey) {
        return webClient.get()
                .uri("https://api.anthropic.com/v1/models")
                .headers(h -> {
                    h.setBearerAuth(apiKey);
                    h.set("anthropic-version", "2023-06-01");
                })
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(8));
    }

    private Mono<Void> pingGoogle(String apiKey) {
        return webClient.get()
                .uri("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(8));
    }

    private Mono<Void> pingGrok(String apiKey) {
        return webClient.get()
                .uri("https://api.x.ai/v1/models")
                .headers(h -> h.setBearerAuth(apiKey))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(8));
    }

    /* -------------------- Helpers -------------------- */
    private void setBearer(HttpHeaders headers, String apiKey) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
    }
}
