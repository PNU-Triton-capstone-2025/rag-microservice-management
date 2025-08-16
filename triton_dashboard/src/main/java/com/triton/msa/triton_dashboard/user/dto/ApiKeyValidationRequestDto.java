package com.triton.msa.triton_dashboard.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;

public record ApiKeyValidationRequestDto(
        LlmProvider provider,
        @JsonProperty("api_key")
        String apiKey
) {
}
