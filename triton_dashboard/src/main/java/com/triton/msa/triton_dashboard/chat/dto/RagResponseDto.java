package com.triton.msa.triton_dashboard.chat.dto;

public record RagResponseDto(
        String response,
        String annotatedSpec,
        String explanation
) {
}
