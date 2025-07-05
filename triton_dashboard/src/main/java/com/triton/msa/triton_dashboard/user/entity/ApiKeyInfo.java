package com.triton.msa.triton_dashboard.user.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ApiKeyInfo {

    private String apiServiceApiKey;

}
