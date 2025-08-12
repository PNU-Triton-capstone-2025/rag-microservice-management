package com.triton.msa.triton_dashboard.chat_history.repository;

import com.triton.msa.triton_dashboard.chat_history.entity.ChatHistory;
import com.triton.msa.triton_dashboard.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByProjectOrderByCreatedAtDesc(Project project);
    void deleteByIdAndProjectId(Long historyId, Long projectId);
}
