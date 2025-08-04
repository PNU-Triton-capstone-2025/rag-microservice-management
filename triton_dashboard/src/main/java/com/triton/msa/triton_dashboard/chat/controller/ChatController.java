package com.triton.msa.triton_dashboard.chat.controller;

import com.triton.msa.triton_dashboard.chat.dto.RagResponseDto;
import com.triton.msa.triton_dashboard.chat.service.ChatHistoryService;
import com.triton.msa.triton_dashboard.chat.service.RagService;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.time.Duration;
import java.util.Map;

@Controller
@RequestMapping("/projects/{projectId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;
    private final ChatHistoryService chatHistoryService;
    private final ProjectService projectService;

    @GetMapping
    public String chatPage(@PathVariable Long projectId, Model model, Principal principal) {
        Project project = projectService.getProject(projectId);

        model.addAttribute("project", project);
        model.addAttribute("history", chatHistoryService.getHistoryForProject(project));

        return "projects/chat";
    }

    @PostMapping("/send")
    public String sendMessage(@PathVariable Long projectId, @RequestParam String query, Model model, Principal principal) {
        Project project = projectService.getProject(projectId);

        RagResponseDto responseDto = ragService.generateDeploymentSpec(principal.getName(), projectId, query);

        model.addAttribute("project", project);
        model.addAttribute("response", responseDto);
        model.addAttribute("query", query);

        model.addAttribute("history", chatHistoryService.getHistoryForProject(project));

        return "projects/chat";
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamChat(
            @PathVariable Long projectId,
            @RequestParam String query) {

        // 1. 프로젝트 조회
        Project project = projectService.getProject(projectId);

        // 2. Gemini API 비동기 호출
        return ragService.generateWithGeminiAsync(projectId, query)
                .flatMapMany(fullResponse -> {

                    // 3. 응답 검증
                    if (fullResponse == null || fullResponse.isBlank()) {
                        fullResponse = "⚠️ 응답이 비어 있습니다.";
                    }

                    // 4. 채팅 내역 저장 (DB)
                    chatHistoryService.saveHistory(project, query, fullResponse);

                    // 5. Flux로 한 글자씩 스트리밍
                    return Flux.fromStream(fullResponse.chars()
                                    .mapToObj(c -> String.valueOf((char) c)))
                            .delayElements(Duration.ofMillis(30));
                })
                // 6. 예외 처리
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Flux.just("⚠️ 요청 실패");
                });
    }
}
