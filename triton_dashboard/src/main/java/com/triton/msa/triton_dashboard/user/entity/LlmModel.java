package com.triton.msa.triton_dashboard.user.entity;

public enum LlmModel {
    GPT_4O("gpt-4o", LlmProvider.OPENAI),
    GPT_3_5("gpt-3.5-turbo", LlmProvider.OPENAI),
    CLAUDE_3_OPUS("claude-3-opus", LlmProvider.ANTHROPIC),
    CLAUDE_3_HAIKU("claude-3-haiku", LlmProvider.ANTHROPIC),
    GEMINI_15_FLASH("gemini-1.5-flash", LlmProvider.GOOGLE),
    GEMINI_PRO("gemini-pro", LlmProvider.GOOGLE),
    GROK_1("grok-1", LlmProvider.GROK);

    private final String modelName;
    private final LlmProvider provider;

    LlmModel(String modelName, LlmProvider provider) {
        this.modelName = modelName;
        this.provider = provider;
    }

    public String getModelName() {
        return modelName;
    }

    public LlmProvider getProvider() {
        return provider;
    }
}
