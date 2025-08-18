package com.triton.msa.triton_dashboard.rag_history.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;

import java.time.LocalDateTime;

public record RagHistoryResponseDto(
    Long id,
    String title,
    @JsonProperty("user_query")
    String userQuery,
    @JsonProperty("llm_response")
    String llmResponse,
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {
    public static RagHistoryResponseDto from(RagHistory entity) {
        return new RagHistoryResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getUserQuery(),
                entity.getLlmResponse(),
                entity.getCreatedAt()
        );
    }
}
