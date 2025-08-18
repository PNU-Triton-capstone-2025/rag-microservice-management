package com.triton.msa.triton_dashboard.rag_history.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RagHistorySaveRequestDto(
        String title,
        @JsonProperty("user_query")
        String userQuery,
        @JsonProperty("llm_response")
        String llmResponse
) {}
