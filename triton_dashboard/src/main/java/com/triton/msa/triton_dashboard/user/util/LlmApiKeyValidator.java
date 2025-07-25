package com.triton.msa.triton_dashboard.user.util;

import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class LlmApiKeyValidator {

    private final RestTemplate restTemplate = new RestTemplate();

    public void validate(String apiKey, LlmModel model) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API 키를 입력해주세요");
        }
        if (model == null || model.getProvider() == null) {
            throw new IllegalArgumentException("모델 또는 공급자를 선택해주세요.");
        }

        String endpoint = getEndpoint(model);
        HttpHeaders headers = buildHeaders(apiKey, model.getProvider());
        String body = getTestBody(model);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            String responseBody = response.getBody();

            if (!response.getStatusCode().is2xxSuccessful()
                    || (responseBody != null && responseBody.contains("\"error\""))) {
                throw new IllegalArgumentException("API 키 인증 실패: " + summaryErrorMsg(responseBody));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("API 키 요청 실패: " + summaryErrorMsg(e.getMessage()));
        }
    }

    private String getEndpoint(LlmModel model) {
        return switch (model.getProvider()) {
            case OPENAI -> "https://api.openai.com/v1/chat/completions";
            case ANTHROPIC -> "https://api.anthropic.com/v1/messages";
            case GOOGLE -> "https://generativelanguage.googleapis.com/v1beta/models/" + model.getModelName() + ":generateContent";
            case GROK -> "https://api.grok.xyz/v1/chat";
        };
    }

    private HttpHeaders buildHeaders(String apiKey, LlmProvider provider) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        switch (provider) {
            case OPENAI, GROK -> headers.set("Authorization", "Bearer " + apiKey);
            case ANTHROPIC -> {
                headers.set("Authorization", "Bearer " + apiKey);
                headers.set("anthropic-version", "2023-06-01");
            }
            case GOOGLE -> headers.set("X-goog-api-key", apiKey);
        }

        return headers;
    }

    private String getTestBody(LlmModel model) {
        return switch (model.getProvider()) {
            case OPENAI, GROK -> """
                {
                  "model": "%s",
                  "messages": [{"role": "user", "content": "ping"}],
                  "temperature": 0.1
                }
            """.formatted(model.getModelName());

            case ANTHROPIC -> """
                {
                  "model": "%s",
                  "messages": [{"role": "user", "content": "ping"}],
                  "max_tokens": 10
                }
            """.formatted(model.getModelName());

            case GOOGLE -> """
                {
                  "contents": [{"parts": [{"text": "ping"}]}]
                }
            """;
        };
    }

    private String summaryErrorMsg(String rawErrorMessage) {
        if (rawErrorMessage.contains("Too Many Requests")) {
            return "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
        } else if (rawErrorMessage.contains("401")) {
            return "API 키가 유효하지 않습니다.";
        } else if (rawErrorMessage.contains("insufficient_quota")) {
            return "API 사용 할당량이 초과되었습니다.";
        } else {
            return "알 수 없는 오류가 발생했습니다.";
        }
    }
}
