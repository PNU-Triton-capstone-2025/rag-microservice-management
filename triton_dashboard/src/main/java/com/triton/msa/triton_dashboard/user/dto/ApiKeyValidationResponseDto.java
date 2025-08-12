package com.triton.msa.triton_dashboard.user.dto;

import java.util.Map;

public record ApiKeyValidationResponseDto(
        Map<String, String> results,
        boolean allValid
) {}
