package com.triton.msa.triton_dashboard.private_data.entity;

import com.triton.msa.triton_dashboard.project.entity.Project;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class PrivateData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String filename;
    private String contentType;

    @Column(nullable = false)
    private Instant createdAt;

    protected PrivateData() {} // JPA 기본 생성자

    public PrivateData(Project project, String filename, String contentType, Instant createdAt) {
        this.project = project;
        this.filename = filename;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }

    public String filename() { return filename; }
}
