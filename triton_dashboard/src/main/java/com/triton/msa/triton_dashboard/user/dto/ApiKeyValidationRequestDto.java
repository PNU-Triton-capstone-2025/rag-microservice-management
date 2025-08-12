package com.triton.msa.triton_dashboard.user.dto;

public record ApiKeyValidationRequestDto(
    String aiServiceApiKey,
    String llmModel
) {
}
