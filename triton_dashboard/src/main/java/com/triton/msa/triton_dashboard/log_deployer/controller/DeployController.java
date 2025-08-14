package com.triton.msa.triton_dashboard.log_deployer.controller;

import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerCustomDto;
import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerRequestDto;
import com.triton.msa.triton_dashboard.log_deployer.service.LogDeployerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/projects/{projectId}/deploy")
@RequiredArgsConstructor
public class DeployController {

    private final LogDeployerService logDeployerService;

    @GetMapping("/download-config")
    public ResponseEntity<byte[]> downloadConfig(@PathVariable Long projectId) {
        byte[] zipBytes = logDeployerService.generateDeploymentZip(new LogDeployerCustomDto(projectId, "logging", 5044));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"log-deploy-config-" + projectId + ".zip\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipBytes.length)
                .body(zipBytes);
    }
}
