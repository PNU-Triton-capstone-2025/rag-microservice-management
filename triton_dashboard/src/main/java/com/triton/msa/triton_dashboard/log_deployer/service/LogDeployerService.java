package com.triton.msa.triton_dashboard.log_deployer.service;

import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerCustomDto;
import com.triton.msa.triton_dashboard.log_deployer.exception.LogDeploymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class LogDeployerService {
    private final String elasticsearchHost;
    private final String elasticsearchPort;

    public LogDeployerService(
            @Value("${elasticsearch.host}")
            String elasticsearchHost,
            @Value("${elasticsearch.port}")
            String elasticsearchPort
    ) {
        this.elasticsearchHost = elasticsearchHost;
        this.elasticsearchPort = elasticsearchPort;
    }

    public byte[] generateDeploymentZip(LogDeployerCustomDto customDto) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos)) {

            Map<String, String> templates = makeTemplate();

            for (Map.Entry<String, String> entry : templates.entrySet()) {
                addContent(zos, entry.getKey(), entry.getValue(), customDto);
            }
            zos.finish();
            return baos.toByteArray();
        }
        catch (IOException ex) {
            throw new LogDeploymentException("로그 배포용 ZIP 파일 생성에 실패했습니다.", ex);
        }
    }

    private static Map<String, String> makeTemplate() {
        Map<String, String> templates = new LinkedHashMap<>();

        templates.put("01-namespace.yml", "log_templates/namespace.yml");
        templates.put("02-filebeat-rbac.yml", "log_templates/filebeat-rbac.yml");
        templates.put("03-filebeat-config.yml", "log_templates/filebeat-config.yml");
        templates.put("04-filebeat-daemonset.yml", "log_templates/filebeat-daemonset.yml");
        templates.put("05-logstash-config.yml", "log_templates/logstash-config.yml");
        templates.put("06-logstash-deployment.yml", "log_templates/logstash-deployment.yml");
        templates.put("07-metricbeat-rbac.yml", "log_templates/metricbeat-rbac.yml");
        templates.put("08-metricbeat-config.yml", "log_templates/metricbeat-config.yml");
        templates.put("09-metricbeat-daemonset.yml", "log_templates/metricbeat-daemonset.yml");
        return templates;
    }

    private void addContent(ZipOutputStream zos, String filename, String resourcePath, LogDeployerCustomDto customDto) throws IOException {
        String processedContent = customizeTemplate(resourcePath, customDto);
        addToZipFromString(zos, filename, processedContent);
    }

    private String customizeTemplate(String resourcePath, LogDeployerCustomDto customDto) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        return template
                .replace("${NAMESPACE}", customDto.namespace())
                .replace("${LOGSTASH_PORT}", String.valueOf(customDto.logstashPort()))
                .replace("${PROJECT_ID}", String.valueOf(customDto.projectId()))
                .replace("${ELASTICSEARCH_HOST}", elasticsearchHost)
                .replace("${ELASTICSEARCH_PORT}", elasticsearchPort);
    }

    private void addToZipFromString(ZipOutputStream zos, String fileName, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(fileName));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
