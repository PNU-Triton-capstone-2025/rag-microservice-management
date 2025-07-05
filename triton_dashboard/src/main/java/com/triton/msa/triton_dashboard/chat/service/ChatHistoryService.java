package com.triton.msa.triton_dashboard.chat.service;

import com.triton.msa.triton_dashboard.chat.entity.ChatHistory;
import com.triton.msa.triton_dashboard.chat.repository.ChatHistoryRepository;
import com.triton.msa.triton_dashboard.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Transactional
    public void saveHistory(Project project, String query, String response) {
        String title = query.substring(0, Math.min(query.length(), 50)) + "...";
        ChatHistory history = new ChatHistory(project, title, query, response);

        chatHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<ChatHistory> getHistoryForProject(Project project) {
        return chatHistoryRepository.findByProjectOrderByCreatedAtDesc(project);
    }
}
