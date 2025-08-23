package com.triton.msa.triton_dashboard.user.dto;

import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import jakarta.validation.constraints.NotNull;

public record UserApiKeyRequestDto(
        @NotNull(message = "LLM Provider를 지정해야 합니다.")
        LlmProvider provider
) {

}
