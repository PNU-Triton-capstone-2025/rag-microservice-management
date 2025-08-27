package com.triton.msa.triton_dashboard.monitoring.dto;

public record RecommendedResourcesDto(
        String cpuRequest,
        String cpuLimit,
        String memoryRequest,
        String memoryLimit
) {
}
