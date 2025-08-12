package com.triton.msa.triton_dashboard.rag.dto;

public record RagResponseDto(
        String response,
        String annotatedSpec,
        String explanation
) {
}
