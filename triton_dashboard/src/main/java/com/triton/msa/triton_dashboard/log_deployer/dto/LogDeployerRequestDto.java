package com.triton.msa.triton_dashboard.log_deployer.dto;

public record LogDeployerRequestDto(
        String namespace,
        Integer logstashPort
) {
}
