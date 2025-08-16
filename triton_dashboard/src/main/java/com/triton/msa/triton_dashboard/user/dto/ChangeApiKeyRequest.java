package com.triton.msa.triton_dashboard.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;

public record ChangeApiKeyRequest(
        LlmProvider provider,
        @JsonProperty("new_api_key")
        String newApiKey
) {
}
