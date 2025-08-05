package com.triton.msa.triton_dashboard.chat.service;

import com.triton.msa.triton_dashboard.chat.dto.RagRequestDto;
import com.triton.msa.triton_dashboard.chat.dto.RagResponseDto;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import com.triton.msa.triton_dashboard.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagService {

    private final RestTemplate restTemplate;
    private final ChatHistoryService chatHistoryService;
    private final ProjectService projectService;
    private final UserService userService;
    private final WebClient webClient;

    @Value("${rag.service.url}")
    private String ragServiceUrl;

    public RagResponseDto generateDeploymentSpec(String username, Long projectId, String query) {
        Project project = projectService.getProject(projectId);

        RagRequestDto requestDto = new RagRequestDto(query);

        RagResponseDto responseDto = restTemplate.postForObject(ragServiceUrl, requestDto, RagResponseDto.class);

        if(responseDto != null) {
            chatHistoryService.saveHistory(project, query, responseDto.response());
        }

        return responseDto;
    }

    public Mono<String> generateWithGeminiAsync(Long projectId, String prompt) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String apiKey = userService.getUser(username).getApiKeyInfo().getApiServiceApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            return Mono.just("⚠️ API 키가 설정되지 않았습니다.");
        }

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
                        return (String) parts.get(0).get("text");
                    } catch (Exception e) {
                        return "⚠️ 응답 파싱 실패";
                    }
                })
                .onErrorReturn("⚠️ 요청 실패");
    }
}