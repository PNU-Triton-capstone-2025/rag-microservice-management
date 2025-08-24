package com.triton.msa.triton_dashboard.monitoring.controller;

import com.triton.msa.triton_dashboard.monitoring.dto.MonitoringHistoryResponseDto;
import com.triton.msa.triton_dashboard.monitoring.service.MonitoringHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/projects/{projectId}/monitoring")
@RequiredArgsConstructor
public class MonitoringHistoryApiController {
    private final MonitoringHistoryService monitoringHistoryService;

    @GetMapping
    public ResponseEntity<Page<MonitoringHistoryResponseDto>> getMonitoringHistories(
            @PathVariable Long projectId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MonitoringHistoryResponseDto> responses = monitoringHistoryService.getMonitoringHistories(projectId, pageable)
                .map(MonitoringHistoryResponseDto::from);

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{monitoringHistoryId}")
    public ResponseEntity<Void> deleteMonitoringHistory(
            @PathVariable Long monitoringHistoryId
    ) {
       monitoringHistoryService.deleteHistory(monitoringHistoryId);

       return ResponseEntity.noContent().build();
    }
}
