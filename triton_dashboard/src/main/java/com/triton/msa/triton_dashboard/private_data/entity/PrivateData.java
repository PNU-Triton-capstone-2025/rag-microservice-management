package com.triton.msa.triton_dashboard.private_data.entity;

import com.triton.msa.triton_dashboard.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
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
}
