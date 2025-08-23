package com.triton.msa.triton_dashboard.monitoring.dto;

import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisEndpoint;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;

public record LogAnalysisEndpointUpdateRequestDto(
        LlmProvider provider,
        LlmModel model
) {

}
