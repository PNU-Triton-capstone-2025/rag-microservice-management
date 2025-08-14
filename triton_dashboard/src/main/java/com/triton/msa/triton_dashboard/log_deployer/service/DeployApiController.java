package com.triton.msa.triton_dashboard.log_deployer.service;

import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/projects/{projectId}/deploy")
@RequiredArgsConstructor
public class DeployApiController {
    private final LogDeployerService logDeployerService;

    @PostMapping("/download-config")
    public ResponseEntity<byte[]> downloadConfig(
            @PathVariable Long projectId,
            @RequestBody LogDeployerRequestDto requestDto
    ) throws IOException {
        byte[] zipBytes = logDeployerService.generateDeploymentZip(projectId, requestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String filename = String.format("log-deploy-%s-config.zip", requestDto.namespace());
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipBytes.length)
                .body(zipBytes);
    }
}
