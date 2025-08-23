package com.triton.msa.triton_dashboard.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.triton.msa.triton_dashboard.monitoring.entity.MonitoringHistory;

import java.time.LocalDateTime;

public record MonitoringHistoryResponseDto(
        Long id,
        String title,
        @JsonProperty("monitoring_report")
        String monitoringReport,
        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
    public static MonitoringHistoryResponseDto from(MonitoringHistory monitoringHistory) {
        return new MonitoringHistoryResponseDto(
                monitoringHistory.getId(),
                monitoringHistory.getTitle(),
                monitoringHistory.getMonitoringReport(),
                monitoringHistory.getCreatedAt()
        );
    }
}
