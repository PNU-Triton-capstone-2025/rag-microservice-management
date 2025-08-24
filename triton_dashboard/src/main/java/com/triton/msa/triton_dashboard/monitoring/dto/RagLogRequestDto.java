package com.triton.msa.triton_dashboard.monitoring.dto;

public record RagLogRequestDto(
        String esIndex,
        String provider,
        String model,
        String query
) {
}
