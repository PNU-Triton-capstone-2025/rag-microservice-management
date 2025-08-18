package com.triton.msa.triton_dashboard.rag_history.dto;

public record RagHistorySaveRequestDto(
        String title,
        String userQuery,
        String llmResponse
) { }
