package com.triton.msa.triton_dashboard.rag_history.repository;

import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;
import com.triton.msa.triton_dashboard.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RagHistoryRepository extends JpaRepository<RagHistory, Long> {
    List<RagHistory> findByProjectOrderByCreatedAtDesc(Project project);
    void deleteByIdAndProjectId(Long historyId, Long projectId);
}
