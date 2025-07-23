package com.triton.msa.triton_dashboard.user.util;

import com.triton.msa.triton_dashboard.user.entity.LlmModel;
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
        String endpoint = switch (model) {
            case OPENAI -> "https://api.openai.com/v1/chat/completions";
            case CLAUDE -> "https://api.anthropic.com/v1/messages";
            case GEMINI -> "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
            case GROK -> "https://api.grok.xyz/v1/chat"; // 가정
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        if (model == LlmModel.CLAUDE) {
            headers.set("anthropic-version", "2023-06-01");
        }

        String body = getTestBody(model);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("API 키 인증 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("API 키 요청 실패: " + e.getMessage());
        }
    }

    private String getTestBody(LlmModel model) {
        return switch (model) {
            case OPENAI, GROK -> """
                {
                  "model": "%s",
                  "messages": [{"role": "user", "content": "ping"}],
                  "temperature": 0.1
                }
            """.formatted(model.getDefaultModelName());

            case CLAUDE -> """
                {
                  "model": "%s",
                  "messages": [{"role": "user", "content": "ping"}],
                  "max_tokens": 10
                }
            """.formatted(model.getDefaultModelName());

            case GEMINI -> """
                {
                  "contents": [{"parts": [{"text": "ping"}]}]
                }
            """;
        };
    }
}
