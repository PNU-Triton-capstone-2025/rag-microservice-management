package com.triton.msa.triton_dashboard.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LlmProvider {
    OPENAI,
    CLAUDE,
    GEMINI;

    @JsonCreator
    public static LlmProvider fromString(String value) {
        return LlmProvider.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
