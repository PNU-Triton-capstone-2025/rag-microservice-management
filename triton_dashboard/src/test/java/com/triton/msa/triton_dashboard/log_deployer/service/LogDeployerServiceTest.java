package com.triton.msa.triton_dashboard.log_deployer.service;

import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerCustomDto;
import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LogDeployerServiceTest {
    private final LogDeployerService logDeployerService = new LogDeployerService("localhost", "9200");

    @Test
    @DisplayName("프로젝트 ID 기반 배포 명세 파일 생성")
    void generateDeploymentZip() throws IOException{
        Long projectId = 123L;

        byte[] zipBytes = logDeployerService.generateDeploymentZip(new LogDeployerCustomDto(projectId, "logging", 5044));

        assertThat(zipBytes).isNotNull();
        assertThat(zipBytes.length).isGreaterThan(0);

        try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            assertThat(zis.getNextEntry().getName()).isEqualTo("01-namespace.yml");
            assertThat(zis.getNextEntry().getName()).isEqualTo("02-filebeat-rbac.yml");
            assertThat(zis.getNextEntry().getName()).isEqualTo("03-filebeat-config.yml");
            assertThat(zis.getNextEntry().getName()).isEqualTo("04-filebeat-daemonset.yml");
            assertThat(zis.getNextEntry().getName()).isEqualTo("05-logstash-config.yml");

            String logstashConfigContent = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(logstashConfigContent).contains("index => \"project-" + projectId + "-logs");

            assertThat(zis.getNextEntry().getName()).isEqualTo("06-logstash-deployment.yml");

            //assertThat(zis.getNextEntry()).isNull();
        }
    }
}
