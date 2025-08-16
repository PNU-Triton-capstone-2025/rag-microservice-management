package com.triton.msa.triton_dashboard.rag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RagRequestDto(
        String provider,
        String model,
        String query,
        @JsonProperty("query_type")
        String queryType
) {}
