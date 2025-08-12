package com.triton.msa.triton_dashboard.chat.controller;

import com.triton.msa.triton_dashboard.chat.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Controller
@RequestMapping("/projects/{projectId}/chat")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> streamChatResponse(@PathVariable Long projectId, @RequestParam String query) {
        return ragService.streamChatResponse(projectId, query);
    }
}
