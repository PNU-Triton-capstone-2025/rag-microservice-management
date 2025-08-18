package com.triton.msa.triton_dashboard.rag.util;

import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import com.triton.msa.triton_dashboard.rag.dto.RagRequestDto;
import com.triton.msa.triton_dashboard.rag.dto.RagResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.user.entity.ApiKeyInfo;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagExecutor {

    private final RestTemplate restTemplate;
    private final RagHistoryService ragHistoryService;
    private final ProjectService projectService;
    private final UserService userService;
    private final WebClient webClient;

    public Mono<String> generateWithGeminiAsync(Long projectId, String prompt) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String apiKey = userService.getUser(username)
                .getApiKeys().stream()
                .filter(k -> k.getProvider() == LlmProvider.GEMINI)
                .map(ApiKeyInfo::getApiKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("GOOGLE API 키가 없습니다."));

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + "gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> {
                    try {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        String text = (String) parts.get(0).get("text");

                        if (text == null || text.isBlank()) {
                            throw new RuntimeException("응답이 비어있습니다.");
                        }
                        return text;
                    } catch (Exception e) {
                        throw new RuntimeException("응답 파싱 실패");
                    }
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("️요청 실패", e)));
    }

    public Flux<String> streamChatResponse(Long projectId, String query) {
        Project project = projectService.getProject(projectId);

        return generateWithGeminiAsync(projectId, query)
                .flatMapMany(fullResponse -> {
                    String[] tokens = fullResponse.split("(?<=\\n)");

                    return Flux.fromArray(tokens)
                            .delayElements(Duration.ofMillis(20))
                            .doOnComplete(() -> ragHistoryService.saveHistory(project, query, fullResponse));
                })
                .onErrorResume(e -> {
                    log.error("LLM 요청 실패", e);
                    return Flux.just("요청 실패");
                });
    }
}