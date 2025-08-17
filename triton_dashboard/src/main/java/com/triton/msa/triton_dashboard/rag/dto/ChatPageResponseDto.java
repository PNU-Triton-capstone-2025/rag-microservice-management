package com.triton.msa.triton_dashboard.rag.dto;

import com.triton.msa.triton_dashboard.project.dto.ProjectResponseDto;
import com.triton.msa.triton_dashboard.rag_history.dto.RagHistoryResponseDto;

import java.util.List;

public record ChatPageResponseDto(
        ProjectResponseDto project,
        List<RagHistoryResponseDto> history
) {

}
