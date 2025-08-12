package com.triton.msa.triton_dashboard.chat.dto;

import com.triton.msa.triton_dashboard.chat.entity.ChatHistory;

import java.util.List;

public record ChatPageResponseDto(
        ProjectResponseDto project,
        List<ChatHistoryResponseDto> history
) {

}
