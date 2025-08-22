package com.triton.msa.triton_dashboard.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonitoringAnalysisDto(
        String title,
        @JsonProperty("llm_response")
        String llmResponse
) {

}