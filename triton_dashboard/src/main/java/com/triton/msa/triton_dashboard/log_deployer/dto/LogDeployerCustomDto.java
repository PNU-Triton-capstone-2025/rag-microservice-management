package com.triton.msa.triton_dashboard.log_deployer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogDeployerCustomDto(
        @JsonProperty("project_id")
        Long projectId,
        String namespace,
        @JsonProperty("logstash_port")
        Integer logstashPort
) {

}
