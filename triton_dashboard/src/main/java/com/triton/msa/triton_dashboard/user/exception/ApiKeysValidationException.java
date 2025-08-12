package com.triton.msa.triton_dashboard.user.exception;

import com.triton.msa.triton_dashboard.user.dto.ApiKeyValidationResponseDto;
import com.triton.msa.triton_dashboard.user.dto.UserRegistrationDto;
import lombok.Getter;

@Getter
public class ApiKeysValidationException extends RuntimeException {
    private final ApiKeyValidationResponseDto results;
    private final UserRegistrationDto userInput;

    public ApiKeysValidationException(ApiKeyValidationResponseDto results,
                                      UserRegistrationDto userInput) {
        super("API 키 검증 실패");
        this.results = results;
        this.userInput = userInput;
    }
}

