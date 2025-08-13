package com.triton.msa.triton_dashboard.rag.dto;

public record RagRequestDto(
        String provider,
        String model,
        String query,
        String queryType
) {}
