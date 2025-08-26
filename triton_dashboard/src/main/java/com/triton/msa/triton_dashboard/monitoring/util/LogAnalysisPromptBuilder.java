package com.triton.msa.triton_dashboard.monitoring.util;

import java.util.List;
import java.util.Map;

public final class LogAnalysisPromptBuilder {
    private LogAnalysisPromptBuilder() {

    }

    public static String buildPrompt(List<Map<String, String>> errorLogs, String resourceSuggestion) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 컨테이너 기반 마이크로서비스 환경의 종합적인 모니터링 분석 요청입니다. 에러 로그와 성능 지표를 검토해서 원인 분석 및 배포 파일 개선 방안을 제시해주세요\n\n");

        if(errorLogs != null && !errorLogs.isEmpty()) {
            sb.append("--- 에러 로그 분석 ---\n");
            sb.append("다음은 각 서비스의 최근 3분간 발생한 에러 로그입니다. \n\n");
        }
        for(Map<String, String> serviceErrorLogs : errorLogs) {
            String serviceName = serviceErrorLogs.get("serviceName");
            String logs = serviceErrorLogs.get("logs");
            sb.append("### 서비스 '").append(serviceName).append("'의 에러 로그 ###\n");
            sb.append("```\n").append(logs).append("\n```\n\n");
        }

        if (resourceSuggestion != null && !resourceSuggestion.isBlank()) {
            sb.append("--- 성능 및 리소스 최적화 제안 ---\n");
            sb.append("다음은 각 서비스의 리소스 사용량을 기반으로 한 최소 보장 리소스, 최대 제한 리소스 추천 값입니다. 이전 배포에서 적용한 최소 보장 리소스, 최대 제한 리소스 값과 30%이상 차이난다면, 리소스 제한 값 수정 방안도 제안해주세요\n\n");
            sb.append(resourceSuggestion);
            sb.append("\n\n");
        }

        sb.append("\n--- 종합 분석 요청 ---\n");
        sb.append("위의 에러 로그와 성능 데이터를 종합적으로 고려했을 때, 문제의 핵심 원인은 무엇이며 어떤 해결 방안을 제안할 수 있나요? 만약 에러와 성능 문제가 연관성이 있다면 함께 설명해주세요.");

        return sb.toString();
    }
}
