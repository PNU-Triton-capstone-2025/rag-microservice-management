package com.triton.msa.triton_dashboard.rag.dto;

public record RagResponseDto(
        String userQuery,
        String response
) {
}
