package com.triton.msa.triton_dashboard.private_data.entity;

import com.triton.msa.triton_dashboard.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
@Table(
        name =  "private_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "filename"}))
public class PrivateData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String filename;

    private String contentType;

    private Instant createdAt;

    protected PrivateData() {}

    public PrivateData(Project project, String filename, String contentType, Instant createdAt) {
        this.project = project;
        this.filename = filename;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }
}
