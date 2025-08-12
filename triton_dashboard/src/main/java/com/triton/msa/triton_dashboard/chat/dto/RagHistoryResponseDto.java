package com.triton.msa.triton_dashboard.chat.dto;

import com.triton.msa.triton_dashboard.rag_history.entity.RagHistory;

import java.time.LocalDateTime;

public record RagHistoryResponseDto(
    Long id,
    String UserQuery,
    String llmResponse,
    LocalDateTime createdAt
) {
    public static RagHistoryResponseDto from(RagHistory entity) {
        return new RagHistoryResponseDto(
                entity.getId(),
                entity.getUserQuery(),
                entity.getLlmResponse(),
                entity.getCreatedAt()
        );
    }
}
