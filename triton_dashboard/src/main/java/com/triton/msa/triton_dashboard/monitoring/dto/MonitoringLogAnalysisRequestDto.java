package com.triton.msa.triton_dashboard.monitoring.dto;

public record MonitoringLogAnalysisRequestDto(
        String provider,
        String model,
        String query
) {
}
