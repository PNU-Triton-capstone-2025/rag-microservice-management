package com.triton.msa.triton_dashboard.user.dto;

import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import jakarta.validation.constraints.NotEmpty;

public record UserRegistrationDto(
        @NotEmpty
        String username,
        @NotEmpty
        String password,
        String aiServiceApiKey,
        LlmModel llmModel
) {
    private final static String DEFAULT_USERNAME = "user_default";
    private final static String DEFAULT_PASSWORD = "";
    private final static String DEFAULT_AISERVICEAPIKEY = "ai service key";
    private final static LlmModel DEFAULT_LLMMODEL = LlmModel.OPENAI;

    public static UserRegistrationDto getEmpty() {
        return new UserRegistrationDto(
                DEFAULT_USERNAME,
                DEFAULT_PASSWORD,
                DEFAULT_AISERVICEAPIKEY,
                DEFAULT_LLMMODEL
        );
    }
}
