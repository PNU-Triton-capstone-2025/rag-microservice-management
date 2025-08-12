package com.triton.msa.triton_dashboard.chat.dto;

import com.triton.msa.triton_dashboard.chat.entity.ChatHistory;

import java.time.LocalDateTime;

public record ChatHistoryResponseDto(
    Long id,
    String UserQuery,
    String llmResponse,
    LocalDateTime createdAt
) {
    public static ChatHistoryResponseDto from(ChatHistory entity) {
        return new ChatHistoryResponseDto(
                entity.getId(),
                entity.getUserQuery(),
                entity.getLlmResponse(),
                entity.getCreatedAt()
        );
    }
}
