package com.triton.msa.triton_dashboard.user.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserRegistrationDto(
        @NotEmpty String username,
        @NotEmpty String password,
        String openaiApiKey,
        String anthropicApiKey,
        String googleApiKey,
        String grokApiKey
) {
    private final static String DEFAULT_USERNAME = "user_default";
    private final static String DEFAULT_PASSWORD = "";

    public static UserRegistrationDto getEmpty() {
        return new UserRegistrationDto(
                DEFAULT_USERNAME,
                DEFAULT_PASSWORD,
                "", "", "", ""
        );
    }
}
