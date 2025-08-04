package com.triton.msa.triton_dashboard.chat.controller;

import com.triton.msa.triton_dashboard.chat.dto.RagResponseDto;
import com.triton.msa.triton_dashboard.chat.entity.ChatHistory;
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

//    @PostMapping("/send")
//    public String sendMessage(@PathVariable Long projectId, @RequestParam String query, Model model, Principal principal) {
//        Project project = projectService.getProject(projectId);
//
//        RagResponseDto responseDto = ragService.generateDeploymentSpec(principal.getName(), projectId, query);
//
//        model.addAttribute("project", project);
//        model.addAttribute("response", responseDto);
//        model.addAttribute("query", query);
//
//        model.addAttribute("history", chatHistoryService.getHistoryForProject(project));
//
//        return "projects/chat";
//    }

    @GetMapping("/history")
    public String chatHistoryList(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProject(projectId);
        model.addAttribute("project", project);
        model.addAttribute("histories", chatHistoryService.getHistoryForProject(project));
        return "projects/chat-history-list";
    }

    @GetMapping("/history/{historyId}")
    public String chatHistoryDetail(@PathVariable Long projectId,
                                    @PathVariable Long historyId,
                                    Model model) {
        Project project = projectService.getProject(projectId);
        ChatHistory history = chatHistoryService.getHistoryById(historyId);
        model.addAttribute("project", project);
        model.addAttribute("history", history);
        return "projects/chat-history-detail";
    }


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamChat(
            @PathVariable Long projectId,
            @RequestParam String query) {

        Project project = projectService.getProject(projectId);

        return ragService.generateWithGeminiAsync(projectId, query)
                .flatMapMany(fullResponse -> {
                    if (fullResponse == null || fullResponse.isBlank()) {
                        fullResponse = "⚠️ 응답이 비어 있습니다.";
                    }

                    // 전체 응답 저장 (DB 저장용)
                    String finalResponse = fullResponse;

                    // 토큰 단위로 쪼개기 (공백 포함, 줄바꿈 포함)
                    String[] tokens = finalResponse.split("(?<= )|(?=\n)");

                    return Flux.fromArray(tokens)
                            .delayElements(Duration.ofMillis(20)) // 토큰 전송 속도
                            .doOnComplete(() -> {
                                // 스트리밍 끝나면 DB 저장
                                chatHistoryService.saveHistory(project, query, finalResponse);
                            });
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Flux.just("⚠️ 요청 실패");
                });
    }
}
