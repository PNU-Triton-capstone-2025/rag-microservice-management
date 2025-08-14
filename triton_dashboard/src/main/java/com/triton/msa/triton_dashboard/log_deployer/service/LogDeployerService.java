package com.triton.msa.triton_dashboard.log_deployer.service;

import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerRequestDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class LogDeployerService {

    public byte[] generateDeploymentZip(Long projectId, LogDeployerRequestDto requestDto) throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos)) {

            Map<String, String> templates = new HashMap<>();

            templates.put("01-namespace.yml", "log_templates/namespace.yml");
            templates.put("02-filebeat-rbac.yml", "log_templates/filebeat-rbac.yml");
            templates.put("03-filebeat-config.yml", "log_templates/filebeat-config.yml");
            templates.put("04-filebeat-daemonset.yml", "log_templates/filebeat-daemonset.yml");
            templates.put("05-logstash-config.yml", "log_templates/logstash-config.yml");
            templates.put("06-logstash-deployment.yml", "log_templates/logstash-deployment.yml");

            for (Map.Entry<String, String> entry : templates.entrySet()) {
                String fileName = entry.getKey();
                String resourcePath = entry.getValue();
                String processedContent = customizeTemplate(resourcePath, projectId, requestDto);
                addToZipFromString(zos, fileName, processedContent);
            }

            /*
            addToZipFromResource(zos, "01-namespace.yml", "log_templates/namespace.yml");
            addToZipFromResource(zos, "02-filebeat-config.yml", "log_templates/filebeat-config.yml");
            addToZipFromResource(zos, "03-filebeat-daemonset.yml", "log_templates/filebeat-daemonset.yml");
            addToZipFromResource(zos, "05-logstash-deployment.yml", "log_templates/logstash-deployment.yml");

            String logstashConfigContent = generateLogstashConfig(projectId);
            addToZipFromString(zos, "04-logstash-config.yml", logstashConfigContent);
            */
            zos.finish();
            return baos.toByteArray();
        }
    }

    private String customizeTemplate(String resourcePath, Long projectId, LogDeployerRequestDto requestDto) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        return template
                .replace("${NAMESPACE}", requestDto.namespace())
                .replace("${LOGSTASH_PORT}", String.valueOf(requestDto.logstashPort()))
                .replace("${PROJECT_ID}", String.valueOf(projectId));
    }

    private String generateLogstashConfig(Long projectId) throws IOException {
        ClassPathResource resource = new ClassPathResource("log_templates/logstash-config.yml");
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
