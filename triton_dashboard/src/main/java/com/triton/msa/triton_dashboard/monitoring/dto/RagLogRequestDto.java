package com.triton.msa.triton_dashboard.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RagLogRequestDto(
        @JsonProperty("es_index")
        String esIndex,
        String provider,
        String model,
        String query
) {
}
