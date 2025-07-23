package com.triton.msa.triton_dashboard.user.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ApiKeyInfo {

    private String apiServiceApiKey;

    @Enumerated(EnumType.STRING)
    private LlmModel llmModel;
}
