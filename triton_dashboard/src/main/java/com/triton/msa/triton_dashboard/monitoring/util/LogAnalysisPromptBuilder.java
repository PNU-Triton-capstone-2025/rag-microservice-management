package com.triton.msa.triton_dashboard.monitoring.util;

import java.util.List;
import java.util.Map;

public final class LogAnalysisPromptBuilder {
    private LogAnalysisPromptBuilder() {

    }

    public static String buildPrompt(List<Map<String, String>> errorLogs) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 컨테이너 기반 마이크로서비스로 구성된 각 서비스의 에러 로그들입니다.");
        for(Map<String, String> serviceErrorLogs : errorLogs) {
            String serviceName = serviceErrorLogs.get("serviceName");
            String logs = serviceErrorLogs.get("logs");
            sb.append("### 서비스 '").append(serviceName).append("'의 최근 3분간 에러 로그 ###\n");
            sb.append("```\n").append(logs).append("\n```\n\n");
        }
        sb.append("\n이 로그들의 핵심 원인은 무엇이며, 어떤 해결 방안을 제안할 수 있나요?");

        return sb.toString();
    }
}
