package com.triton.msa.triton_dashboard.monitoring.dto;

public record ResourceMetricDto(
        double avgCpu,
        double minCpu,
        double maxCpu,
        double avgMemoryBytes,
        double minMemoryBytes,
        double maxMemoryBytes
) {
    public static ResourceMetricDto getEmpty() {
        return new ResourceMetricDto(0, 0, 0, 0, 0, 0);
    }
}
