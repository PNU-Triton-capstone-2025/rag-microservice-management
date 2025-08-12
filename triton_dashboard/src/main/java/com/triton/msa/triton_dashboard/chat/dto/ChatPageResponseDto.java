package com.triton.msa.triton_dashboard.chat.dto;

import java.util.List;

public record ChatPageResponseDto(
        ProjectResponseDto project,
        List<RagHistoryResponseDto> history
) {

}
