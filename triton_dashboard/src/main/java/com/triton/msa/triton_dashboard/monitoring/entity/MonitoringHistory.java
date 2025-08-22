package com.triton.msa.triton_dashboard.monitoring.entity;

import com.triton.msa.triton_dashboard.project.entity.Project;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class MonitoringHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String title;

    @Lob
    private String monitoringReport;
    private LocalDateTime createdAt;

    protected MonitoringHistory() {

    }
    public MonitoringHistory(Project project, String title, String monitoringReport, LocalDateTime createdAt) {
        this.project = project;
        this.title = title;
        this.monitoringReport = monitoringReport;
        this.createdAt = createdAt;
    }
}
