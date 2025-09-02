package com.triton.msa.triton_dashboard.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceAnalysisRequestDto(
        @JsonProperty("es_index")
        String esIndex,
        String provider,
        String model,
        @JsonProperty("api_key")
        String apiKey,
        @JsonProperty("resource_usage")
        String resourceUsage,
        String yamls
) {
}
