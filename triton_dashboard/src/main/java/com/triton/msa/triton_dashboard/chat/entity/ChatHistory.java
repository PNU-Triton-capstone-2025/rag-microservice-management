package com.triton.msa.triton_dashboard.chat.entity;

import com.triton.msa.triton_dashboard.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String title;

    @Lob
    private String userQuery;

    @Lob
    private String llmResponse;

    private LocalDateTime createdAt;

    public ChatHistory(Project project, String title, String userQuery, String llmResponse) {
        this.project = project;
        this.title = title;
        this.userQuery = userQuery;
        this.llmResponse = llmResponse;
        this.createdAt = LocalDateTime.now();
    }

    public ChatHistory() {}
}