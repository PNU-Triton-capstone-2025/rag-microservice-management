package com.triton.msa.triton_dashboard.project.repository;

import com.triton.msa.triton_dashboard.project.entity.ChatHistory;
import com.triton.msa.triton_dashboard.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByProjectOrderByCreatedAtDesc(Project project);
}
