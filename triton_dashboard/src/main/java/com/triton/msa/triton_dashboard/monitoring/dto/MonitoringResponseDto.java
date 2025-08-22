package com.triton.msa.triton_dashboard.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.triton.msa.triton_dashboard.monitoring.entity.MonitoringHistory;

import java.time.LocalDateTime;

public record MonitoringResponseDto(
        Long id,
        String title,
        @JsonProperty("monitoring_report")
        String monitoringReport,
        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
    public static MonitoringResponseDto from(MonitoringHistory monitoringHistory) {
        return new MonitoringResponseDto(
                monitoringHistory.getId(),
                monitoringHistory.getTitle(),
                monitoringHistory.getMonitoringReport(),
                monitoringHistory.getCreatedAt()
        );
    }
}
