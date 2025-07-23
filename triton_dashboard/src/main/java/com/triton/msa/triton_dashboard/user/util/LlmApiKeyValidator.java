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
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API 키를 입력해주세요.");
        }
        if (model == null) {
            throw new IllegalArgumentException("모델을 선택해주세요.");
        }

        String endpoint = switch (model) {
            case OPENAI -> "https://api.openai.com/v1/chat/completions";
            case CLAUDE -> "https://api.anthropic.com/v1/messages";
            case GEMINI -> "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
            case GROK -> "https://api.grok.xyz/v1/chat"; // 가정
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        switch (model) {
            case OPENAI, GROK -> headers.set("Authorization", "Bearer " + apiKey);
            case CLAUDE -> {
                headers.set("Authorization", "Bearer " + apiKey);
                headers.set("anthropic-version", "2023-06-01");
            }
            case GEMINI -> headers.set("X-goog-api-key", apiKey);
        }

        String body = getTestBody(model);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            String responseBody = response.getBody();

            if (!response.getStatusCode().is2xxSuccessful()
                    || (responseBody != null && responseBody.contains("\"error\""))) {

                String shortMsg = "API 키 인증 실패";
                String detailed = responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
                throw new IllegalArgumentException(shortMsg + "\n상세: " + detailed);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("API 키 요청 실패: " + summaryErrorMsg(e.getMessage()) + "\n상세: " + e.getMessage());
        }

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
