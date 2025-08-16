package com.triton.msa.triton_dashboard.rag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RagResponseDto(
        @JsonProperty("user_query")
        String userQuery,
        String response
) {
}
