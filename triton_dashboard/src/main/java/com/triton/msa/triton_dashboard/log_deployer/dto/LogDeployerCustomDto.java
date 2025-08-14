package com.triton.msa.triton_dashboard.log_deployer.dto;

public record LogDeployerCustomDto(
        Long projectId,
        String namespace,
        Integer logstashPort
) {

}
