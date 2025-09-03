package com.triton.msa.triton_dashboard.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RagLogResponseDto(
        String title,
        @JsonProperty("answer")
        String answer
) {

}