package com.triton.msa.triton_dashboard.monitoring.entity;

import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LogAnalysisEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LlmProvider provider;
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

    protected LogAnalysisEndpoint() {

    }

    public LogAnalysisEndpoint(LlmProvider provider, LlmModel model) {
        this.provider = provider;
        this.model = model;
    }
}
