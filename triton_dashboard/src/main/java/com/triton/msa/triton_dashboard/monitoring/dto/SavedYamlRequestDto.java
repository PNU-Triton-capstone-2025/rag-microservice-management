package com.triton.msa.triton_dashboard.monitoring.dto;

public record SavedYamlRequestDto(
        String filename,
        String yamlContent
) {}
