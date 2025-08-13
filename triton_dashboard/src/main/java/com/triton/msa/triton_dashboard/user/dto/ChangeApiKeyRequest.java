package com.triton.msa.triton_dashboard.user.dto;

import com.triton.msa.triton_dashboard.user.entity.LlmProvider;

public record ChangeApiKeyRequest(
        LlmProvider provider,
        String newApiKey
) {
}
