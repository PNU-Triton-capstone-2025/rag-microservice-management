package com.triton.msa.triton_dashboard.monitoring.controller;

import com.triton.msa.triton_dashboard.monitoring.dto.LogAnalysisModelResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.LogAnalysisModelUpdateRequestDto;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisModelService;
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
public class LogAnalysisModelController {
    private final LogAnalysisModelService endpointService;

    @GetMapping
    public ResponseEntity<LogAnalysisModelResponseDto> getEndpoint(@PathVariable Long projectId) {
        LogAnalysisModel endpoint = endpointService.getAnalysisModel(projectId);

        return ResponseEntity.ok(LogAnalysisModelResponseDto.from(endpoint));
    }

    @PutMapping
    public ResponseEntity<Void> updateEndpoint(
            @PathVariable Long projectId,
            @Valid @RequestBody LogAnalysisModelUpdateRequestDto requestDto
    ) {
        endpointService.updateAnalysisModel(projectId, requestDto);

        return ResponseEntity.noContent().build();
    }
}
