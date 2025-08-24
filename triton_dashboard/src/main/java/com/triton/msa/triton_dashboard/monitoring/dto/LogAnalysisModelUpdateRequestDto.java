package com.triton.msa.triton_dashboard.monitoring.dto;

import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import jakarta.validation.constraints.NotNull;

public record LogAnalysisModelUpdateRequestDto(
        @NotNull
        LlmProvider provider,
        @NotNull
        LlmModel model
) {

}
