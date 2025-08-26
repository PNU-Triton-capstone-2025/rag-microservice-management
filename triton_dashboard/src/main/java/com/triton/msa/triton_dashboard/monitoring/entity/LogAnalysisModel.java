package com.triton.msa.triton_dashboard.monitoring.entity;

import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class LogAnalysisModel {
    @Enumerated(EnumType.STRING)
    private LlmProvider provider;
    @Enumerated(EnumType.STRING)
    private LlmModel model;

    public void update(LlmProvider provider, LlmModel model) {
        this.provider = provider;
        this.model = model;
    }

    public LlmProvider fetchProvider() {
        return this.provider;
    }

    public LlmModel fetchModel() {
        return this.model;
    }

    protected LogAnalysisModel() {

    }

    public LogAnalysisModel(LlmProvider provider, LlmModel model) {
        this.provider = provider;
        this.model = model;
    }
}
