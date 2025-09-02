package com.triton.msa.triton_dashboard.monitoring.util;

import com.triton.msa.triton_dashboard.project.entity.SavedYaml;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public final class LogAnalysisPromptBuilder {
    private LogAnalysisPromptBuilder() {

    }

    public static String buildPrompt(List<Map<String, String>> errorLogs, String resourceSuggestion, List<SavedYaml> savedYamls) {
        log.info("make monitoring prompt...");
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 컨테이너 기반 마이크로서비스 환경의 종합적인 모니터링 분석 요청입니다. 에러 로그와 성능 지표를 검토해서 원인 분석 및 배포 파일 개선 방안을 제시해주세요\n\n");

        if(errorLogs != null && !errorLogs.isEmpty()) {
            sb.append("--- 에러 로그 ---\n");
            sb.append("다음은 각 서비스의 최근 3분간 발생한 에러 로그입니다. \n\n");
        }
        for(Map<String, String> serviceErrorLogs : errorLogs) {
            String serviceName = serviceErrorLogs.get("serviceName");
            String logs = serviceErrorLogs.get("logs");
            sb.append("### 서비스 '").append(serviceName).append("'의 에러 로그 ###\n");
            sb.append("```\n").append(logs).append("\n```\n\n");
        }

        if (resourceSuggestion != null && !resourceSuggestion.isBlank()) {
            sb.append("--- 리소스 ---\n");
            sb.append("다음은 각 서비스의 리소스 사용량 값입니다.\n\n");
            sb.append(resourceSuggestion);
            sb.append("\n\n");
        }

        if(savedYamls != null && !savedYamls.isEmpty()) {
            sb.append("--- 현재 배포된 YAML 명세 ---\n");
            for(SavedYaml yaml : savedYamls) {
                sb.append("### 파일명: ").append(yaml.getFileName()).append(" ###\n");
                sb.append("```yaml\n").append(yaml.getYamlContent()).append("\n```\n\n");
            }
        }

        log.info("monitoring prompt completed");
        return sb.toString();
    }
}
