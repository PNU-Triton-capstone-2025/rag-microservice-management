package com.triton.msa.triton_dashboard.user.entity;

public enum LlmModel {
    GPT_4O("gpt-4o", LlmProvider.OPENAI),
    GPT_3_5("gpt-3.5-turbo", LlmProvider.OPENAI),
    CLAUDE_3_OPUS("claude-3-opus", LlmProvider.CLAUDE),
    CLAUDE_3_HAIKU("claude-3-haiku", LlmProvider.CLAUDE),
    GEMINI_15_FLASH("gemini-1.5-flash", LlmProvider.GEMINI),
    GEMINI_PRO("gemini-pro", LlmProvider.GEMINI);

    private final String modelName;
    private final LlmProvider provider;

    LlmModel(String modelName, LlmProvider provider) {
        this.modelName = modelName;
        this.provider = provider;
    }

    public String getModelName() {
        return modelName;
    }

    public String getName() {
        return this.name();
    }

    public LlmProvider getProvider() {
        return provider;
    }
}
