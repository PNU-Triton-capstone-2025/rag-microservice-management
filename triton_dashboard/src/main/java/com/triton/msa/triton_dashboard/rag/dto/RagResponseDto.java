package com.triton.msa.triton_dashboard.rag.dto;

import java.util.List;

public record RagResponseDto(
        String questing,
        String answer,
        List<String> sources,
        String log
) {}
