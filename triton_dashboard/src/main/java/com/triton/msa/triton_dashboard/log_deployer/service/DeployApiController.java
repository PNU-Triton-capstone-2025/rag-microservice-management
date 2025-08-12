package com.triton.msa.triton_dashboard.log_deployer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/projects/{projectId}/deploy")
@RequiredArgsConstructor
public class DeployApiController {
    private final LogDeployerService logDeployerService;

    @GetMapping("/download-config")
    public ResponseEntity<byte[]> downloadConfig(@PathVariable Long projectId) throws IOException {
        byte[] zipBytes = logDeployerService.generateDeploymentZip(projectId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "log-deploy-config-" + projectId + ".zip");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipBytes.length)
                .body(zipBytes);
    }
}
