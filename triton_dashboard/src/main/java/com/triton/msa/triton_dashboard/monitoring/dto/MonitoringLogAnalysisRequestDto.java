package com.triton.msa.triton_dashboard.monitoring.dto;

public record MonitoringLogAnalysisRequestDto(
        String esIndex,
        String provider,
        String model,
        String query
) {
}
