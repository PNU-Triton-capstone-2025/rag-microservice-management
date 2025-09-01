package com.triton.msa.triton_dashboard.monitoring.controller;

import com.triton.msa.triton_dashboard.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;


}
