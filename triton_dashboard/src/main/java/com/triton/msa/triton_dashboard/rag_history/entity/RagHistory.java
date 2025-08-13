package com.triton.msa.triton_dashboard.rag_history.entity;

import com.triton.msa.triton_dashboard.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class RagHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String title;

    @Lob
    private String userQuery;

    @Lob
    private String llmResponse;

    private LocalDateTime createdAt;
    
    public RagHistory(Project project, String title, String userQuery, String llmResponse) {
        this.project = project;
        this.title = title;
        this.userQuery = userQuery;
        this.llmResponse = llmResponse;
        this.createdAt = LocalDateTime.now();
    }

    public RagHistory() {}
}