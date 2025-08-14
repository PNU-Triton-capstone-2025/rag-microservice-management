package com.triton.msa.triton_dashboard.log_deployer.service;

import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerRequestDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class LogDeployerService {

    public byte[] generateDeploymentZip(Long projectId, LogDeployerRequestDto requestDto) throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos)) {
            addToZipFromResource(zos, "01-namespace.yaml", "log_templates/namespace.yaml");
            addToZipFromResource(zos, "02-filebeat-config.yml", "log_templates/filebeat-config.yml");
            addToZipFromResource(zos, "03-filebeat-daemonset.yml", "log_templates/filebeat-daemonset.yml");
            addToZipFromResource(zos, "05-logstash-deployment.yaml", "log_templates/logstash-deployment.yaml");

            String logstashConfigContent = generateLogstashConfig(projectId);
            addToZipFromString(zos, "04-logstash-config.yaml", logstashConfigContent);

            zos.finish();
            return baos.toByteArray();
        }
    }

    private String generateLogstashConfig(Long projectId) throws IOException {
        ClassPathResource resource = new ClassPathResource("log_templates/logstash-config.yaml");
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return template.formatted(projectId);
    }

    private void addToZipFromResource(ZipOutputStream zos, String fileName, String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);

        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);
        StreamUtils.copy(resource.getInputStream(), zos);
        zos.closeEntry();
    }

    private void addToZipFromString(ZipOutputStream zos, String fileName, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(fileName));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
