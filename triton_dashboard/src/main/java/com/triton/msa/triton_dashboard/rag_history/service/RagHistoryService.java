package com.triton.msa.triton_dashboard.rag_history.service;

import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.rag_history.repository.RagHistoryRepository;
import com.triton.msa.triton_dashboard.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagHistoryService {

    private final RagHistoryRepository chatHistoryRepository;

    @Transactional
    public void saveHistory(Project project, String query, String response) {
        int maxLen = 20;
        String title = query.substring(0, Math.min(query.length(), maxLen));
        if (query.length() > maxLen) title += "...";
        RagHistory history = new RagHistory(project, title, query, response);

        chatHistoryRepository.save(history);
    }

    public List<RagHistory> getHistoryForProject(Project project) {
        return chatHistoryRepository.findByProjectOrderByCreatedAtDesc(project);
    }

    public RagHistory getHistoryById(Long id) {
        return chatHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅 이력을 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteHistory(Long historyId, Long projectId) {
        chatHistoryRepository.deleteByIdAndProjectId(historyId, projectId);
    }
}