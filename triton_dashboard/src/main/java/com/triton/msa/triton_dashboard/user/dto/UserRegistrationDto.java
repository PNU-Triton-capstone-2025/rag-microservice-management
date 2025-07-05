package com.triton.msa.triton_dashboard.user.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserRegistrationDto(
        @NotEmpty
        String username,
        @NotEmpty
        String password,
        String aiServiceApiKey
) {
    private final static String DEFAULT_USERNAME = "user_default";
    private final static String DEFAULT_PASSWORD = "";
    private final static String DEFAULT_AISERVICEAPIKEY = "ai service key";

    public static UserRegistrationDto getEmpty() {
        return new UserRegistrationDto(
                DEFAULT_USERNAME,
                DEFAULT_PASSWORD,
                DEFAULT_AISERVICEAPIKEY
        );
    }
}
