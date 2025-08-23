package com.triton.msa.triton_dashboard.monitoring.controller;

import com.triton.msa.triton_dashboard.monitoring.dto.LogAnalysisEndpointUpdateRequestDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisEndpoint;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisEndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/projects/{projectId}/monitoring/endpoint")
@RequiredArgsConstructor
public class LogAnalysisEndpointController {
    private final LogAnalysisEndpointService endpointService;

    @GetMapping
    public ResponseEntity<LogAnalysisEndpoint> getEndpoint(@PathVariable Long projectid) {
        LogAnalysisEndpoint endpoint = endpointService.getEndpoint(projectid);

        return ResponseEntity.ok(endpoint);
    }

    @PutMapping
    public ResponseEntity<Void> updateEndpoint(
            @PathVariable Long projectId,
            @Valid @RequestBody LogAnalysisEndpointUpdateRequestDto requestDto
    ) {
        endpointService.updateEndpoint(projectId, requestDto);

        return ResponseEntity.noContent().build();
    }
}
