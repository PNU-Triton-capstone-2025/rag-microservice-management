package com.triton.msa.triton_dashboard.user.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Embeddable
@Getter
public class ApiKeyInfo {

    private String apiKey; // 추후 암호화 고려. (AES)

    @Enumerated(EnumType.STRING)
    private LlmProvider provider;

    protected ApiKeyInfo() {}

    public ApiKeyInfo(String apiKey, LlmProvider provider) {
        this.apiKey = apiKey;
        this.provider = provider;
    }
}
