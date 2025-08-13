package com.triton.msa.triton_dashboard.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Embeddable
@Getter
public class ApiKeyInfo {

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private LlmProvider provider;

    @Column(name = "api_key", nullable = false)
    private String apiKey; // 추후 암호화 고려. (AES)

    protected ApiKeyInfo() {}

    public ApiKeyInfo(String apiKey, LlmProvider provider) {
        this.apiKey = apiKey;
        this.provider = provider;
    }
}
