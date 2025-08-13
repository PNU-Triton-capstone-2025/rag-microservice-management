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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmApiKeyValidator {

    private final WebClient webClient;

    public void validateAll(UserRegistrationDto dto) {
        Map<String, Object> results = new LinkedHashMap<>();
        boolean allValid = true;

       for (LlmProvider p : LlmProvider.values()) {
           String apiKey = dto.apiKeyOf(p);
           allValid &= validateOne(p.name(), p, apiKey, results);
       }

        if (!allValid) throw new ApiKeysValidationException(new ApiKeyValidationResponseDto(results), dto);
    }

    // 내부 호출용. 비어있으면 스킵, 아니면 provider 별 ping 수행
    private boolean validateOne(String name, LlmProvider provider, String apiKey, Map<String, Object> results) {
        if (apiKey == null || apiKey.isBlank()) {
            results.put(name, "skipped");
            return true;
        }
        try {
            switch (provider) {
                case OPENAI -> pingOpenAI(apiKey);
                case ANTHROPIC -> pingAnthropic(apiKey);
                case GOOGLE -> pingGoogle(apiKey);
                case GROK -> pingGrok(apiKey);
            }
            results.put(name, "valid");
            return true;
        } catch (Exception e) {
            results.put(name, e);
            return false;
        }
    }

    // 외부 호출용. (API 키 변경 등)
    public void validateOne(LlmProvider provider, String apiKey) {
        boolean isValid = validateOne(provider.name(), provider, apiKey, new LinkedHashMap<>());
        if (!isValid) {
            throw new InvalidApiKeyException("API 키 검증에 실패했습니다.");
        }
    }

    /** OpenAI: GET /v1/models (Authorization: Bearer) */
    private void pingOpenAI(String apiKey) {
        webClient.get()
                .uri("https://api.openai.com/v1/models")
                .headers(h -> setBearer(h, apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(8))
                .block(); // 실패 시 예외 throw
    }

    /** Anthropic: GET /v1/models (Authorization + anthropic-version) */
    private void pingAnthropic(String apiKey) {
        webClient.get()
                .uri("https://api.anthropic.com/v1/models")
                .headers(h -> {
                    setBearer(h, apiKey);
                    h.set("anthropic-version", "2023-06-01");
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(8))
                .block();
    }

    /** Google (Gemini): GET /v1beta/models?key= */
    private void pingGoogle(String apiKey) {
        webClient.get()
                .uri("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(8))
                .block();
    }

    /** Grok(xAI): GET /v1/models (Authorization: Bearer) */
    private void pingGrok(String apiKey) {
        webClient.get()
                .uri("https://api.x.ai/v1/models")
                .headers(h -> setBearer(h, apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(8))
                .block();
    }

    /* -------------------- Helpers -------------------- */
    private void setBearer(HttpHeaders headers, String apiKey) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
    }
}
