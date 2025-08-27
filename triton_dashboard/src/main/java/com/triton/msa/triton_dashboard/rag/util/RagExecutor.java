package com.triton.msa.triton_dashboard.rag.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triton.msa.triton_dashboard.rag.dto.RagRequestDto;
import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagExecutor {

    private final RagHistoryService ragHistoryService;
    private final ProjectService projectService;
    @Qualifier("ragWebClient") private final WebClient ragWebClient;
    private final ObjectMapper objectMapper;

    /*
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
    */

    public Flux<String> streamChatResponse(Long projectId, RagRequestDto requestDto) {
        Project project = projectService.getProject(projectId);
        String indexName = "project-" + projectId;

        // rag-server에 보낼 요청 본문(payload) 생성
        Map<String, Object> payload = Map.of(
                "query", requestDto.query(),
                "es_index", indexName,
                "query_type", requestDto.queryType(),
                "provider", requestDto.provider(),
                "model", requestDto.model()
        );

        // rag-server의 스트리밍 API 호출
        Flux<String> tokenStream = ragWebClient.post()
                .uri("/api/get-rag-response-stream") // rag-server의 스트리밍 엔드포인트
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class) // 응답을 String 라인 단위로 받음
                .flatMap(line -> {
                    // rag-server가 보내주는 jsonl 형식의 각 라인을 파싱
                    try {
                        Map<String, Object> data = objectMapper.readValue(line, Map.class);
                        if ("token".equals(data.get("event"))) {
                            // "data" 필드의 값을 문자열로 변환하여 스트림으로 전달
                            return Flux.just(String.valueOf(data.getOrDefault("data", "")));
                        }
                    } catch (IOException e) {
                        log.error("스트림 데이터 파싱 실패: {}", line, e);
                    }
                    return Flux.empty(); // token 이벤트가 아니면 무시
                });

        // 스트림이 완료된 후 DB에 저장하는 로직
        return tokenStream
                .collectList() // 스트림의 모든 토큰을 리스트로 수집
                .flatMapMany(tokens -> {
                    // 수집된 토큰들을 합쳐서 전체 응답 문자열 생성
                    String fullResponse = String.join("", tokens);
                    // 히스토리 저장
                    ragHistoryService.saveHistory(project, requestDto.query(), fullResponse);
                    // 다시 Flux 스트림으로 변환하여 반환
                    return Flux.fromIterable(tokens);
                })
                .onErrorResume(e -> {
                    log.error("RAG 스트림 요청 실패", e);
                    return Flux.just("요청에 실패했습니다. 시스템 로그를 확인해주세요.");
                });
    }
}