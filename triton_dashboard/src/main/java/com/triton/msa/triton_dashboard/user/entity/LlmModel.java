package com.triton.msa.triton_dashboard.user.entity;

public enum LlmModel {
    OPENAI("gpt-3.5-turbo"),
    CLAUDE("claude-instant-1"),
    GEMINI("gemini-pro"),
    GROK("grok-1");

    private final String defaultModelName;

    LlmModel(String defaultModelName) {
        this.defaultModelName = defaultModelName;
    }

    public String getDefaultModelName() {
        return defaultModelName;
    }
}
