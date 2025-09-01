package com.triton.msa.triton_dashboard.rag.util;

import com.triton.msa.triton_dashboard.rag.dto.RagRequestDto;
import com.triton.msa.triton_dashboard.rag.exception.FileUploadException;
import com.triton.msa.triton_dashboard.rag_history.service.RagHistoryService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.user.dto.UserApiKeyRequestDto;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import com.triton.msa.triton_dashboard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagExecutor {

    private final RagHistoryService ragHistoryService;
    private final UserService userService;
    private final ProjectService projectService;
    @Qualifier("ragWebClient") private final WebClient ragWebClient;
  
    public Flux<String> streamChatResponse(Long projectId, RagRequestDto requestDto, MultipartFile file) {
        Project project = projectService.getProject(projectId);
        String indexName = "project-" + projectId;
        String username = userService.getUserByProjectId(projectId).getUsername();
        String userApiKey = userService.getCurrentUserApiKey(username, new UserApiKeyRequestDto(LlmProvider.valueOf(requestDto.provider().toUpperCase())));

        Map<String, Object> payload = new HashMap<>(Map.of(
                "query", requestDto.query(),
                "es_index", indexName,
                "query_type", requestDto.queryType(),
                "provider", requestDto.provider(),
                "model", requestDto.model(),
                "api_key", userApiKey
        ));

        if (file != null && !file.isEmpty()) {
            try {
                String fileContent = new String(file.getBytes(), StandardCharsets.UTF_8);
                payload.put("query", payload.get("query") + "\n\n" + fileContent);
            } catch (IOException e) {
                throw new FileUploadException(e.getMessage());
            }
        }

        return ragWebClient.post()
                .uri("/api/get-rag-response")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMapMany(responseMap -> {
                    String fullResponse = (String) responseMap.getOrDefault("answer", "답변을 생성하지 못했습니다.");

                    ragHistoryService.saveHistory(project, requestDto.query(), fullResponse);

                    String[] tokens = fullResponse.split("(?<=\\n)");

                    // 쪼개진 텍스트(토큰) 배열을 Flux 스트림으로 변환하고,
                    // 각 토큰 사이에 약간의 지연시간을 주어 타이핑 효과를 만듭니다.
                    return Flux.fromArray(tokens)
                            .delayElements(Duration.ofMillis(50)); // 타이핑 속도 조절
                })
                .onErrorResume(e -> {
                    log.error("RAG 요청 처리 중 오류 발생", e);
                    return Flux.just("요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                });
    }
}