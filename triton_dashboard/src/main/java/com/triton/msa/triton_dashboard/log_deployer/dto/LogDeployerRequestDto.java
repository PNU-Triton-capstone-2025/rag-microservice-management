package com.triton.msa.triton_dashboard.log_deployer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogDeployerRequestDto(
        String namespace,
        @JsonProperty("logstash_port")
        Integer logstashPort
) {
}
