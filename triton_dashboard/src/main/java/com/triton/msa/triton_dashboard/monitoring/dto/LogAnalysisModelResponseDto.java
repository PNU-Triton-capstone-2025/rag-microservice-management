package com.triton.msa.triton_dashboard.monitoring.dto;

import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;

public record LogAnalysisModelResponseDto(
        LlmProvider provider,
        LlmModel model
) {
    public static LogAnalysisModelResponseDto from(LogAnalysisModel model) {
        return new LogAnalysisModelResponseDto(
                model.fetchProvider(),
                model.fetchModel()
        );
    }
}
