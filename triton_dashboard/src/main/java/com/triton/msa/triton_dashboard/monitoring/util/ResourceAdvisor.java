package com.triton.msa.triton_dashboard.monitoring.util;

import com.triton.msa.triton_dashboard.monitoring.dto.RecommendedResourcesDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceMetricDto;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ResourceAdvisor {
    private static final double RECOMMENDATION_BUFFER = 1.2;
    private static final double COMPARISON_THRESHOLD = 0.3;

    public RecommendedResourcesDto recommendResources(ResourceMetricDto metricDto) {
        // CPU 최소 보장: 평균 사용량 (코어 단위)
        String cpuRequest = String.format(Locale.US, "%.2fm", metricDto.avgCpu() * 1000);
        // CPU 최대 제한: 최대 사용량에 20% 버퍼 추가
        String cpuLimit = String.format(Locale.US, "%.2fm", metricDto.maxCpu() * 1000 * RECOMMENDATION_BUFFER);

        // 메모리 최소 보장 : 평균 사용량 기준 (MI 단위)
        String memoryRequest = String.format(Locale.US, "%.0Mi", metricDto.avgMemoryBytes() / (1024 * 1024));
        // 메모리 최대 제한 : 최대 사용량에 20% 버퍼 추가
        String memoryLimit = String.format(Locale.US, "%.0Mi", metricDto.maxMemoryBytes() / (1024 * 1024) * RECOMMENDATION_BUFFER);

        return new RecommendedResourcesDto(cpuRequest, cpuLimit, memoryRequest, memoryLimit);
    }

    public String generatePerformancePrompt(String serviceName, ResourceMetricDto metricDto) {
        return String.format(Locale.US,
                "### 서비스 '%s'의 리소스 사용량 ###\n" +
                        "- CPU: 평균 %.2fm, 최대 %.2fm\n" +
                        "- Memory: 평균 %.0fMi, 최대 %.0fMi\n",
                serviceName,
                metricDto.avgCpu() * 1000,
                metricDto.maxCpu() * 1000,
                metricDto.avgMemoryBytes() / (1024 * 1024),
                metricDto.maxMemoryBytes() / (1024 * 1024)
        );
    }
}
