package com.triton.msa.triton_dashboard.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import jakarta.validation.constraints.NotEmpty;

import java.util.EnumMap;
import java.util.Map;

public record UserRegistrationDto(
        @NotEmpty String username,
        @NotEmpty String password,
        @JsonProperty("api_keys")
        Map<LlmProvider, String> apiKeys
) {
    private final static String DEFAULT_USERNAME = "user_default";
    private final static String DEFAULT_PASSWORD = "";
    private final static String DEFAULT_API_KEY = "";

    public String apiKeyOf(LlmProvider provider) {
        return apiKeys == null ? "" : apiKeys.getOrDefault(provider, DEFAULT_API_KEY);
    }

    public static UserRegistrationDto getEmpty() {
        EnumMap<LlmProvider, String> map = new EnumMap<>(LlmProvider.class);
        for (LlmProvider p : LlmProvider.values()) map.put(p, DEFAULT_API_KEY);

        return new UserRegistrationDto(DEFAULT_USERNAME, DEFAULT_PASSWORD, map);
    }
}
