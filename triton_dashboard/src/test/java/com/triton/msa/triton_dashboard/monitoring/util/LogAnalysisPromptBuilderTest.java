package com.triton.msa.triton_dashboard.monitoring.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LogAnalysisPromptBuilderTest {
    @Test
    @DisplayName("에러 로그 목록 프롬프트 생성 테스트")
    void buildPrompt() {
        // given
        Map<String, String> serviceALogs = Map.of(
                "serviceName", "auth-service",
                "logs", "NullPointerException at line 50\nTimeoutException at line 100"
        );
        Map<String, String> serviceBLogs = Map.of(
                "serviceName", "order-service",
                "logs", "DatabaseConnectionException"
        );
        List<Map<String, String>> errorLogs = List.of(serviceALogs, serviceBLogs);

        // when
        String prompt = LogAnalysisPromptBuilder.buildPrompt(errorLogs);

        // then
        assertThat(prompt).contains("다음은 컨테이너 기반 마이크로서비스로 구성된 각 서비스의 에러 로그들입니다.");
        assertThat(prompt).contains("### 서비스 'auth-service'의 최근 3분간 에러 로그 ###");
        assertThat(prompt).contains("```\nNullPointerException at line 50\nTimeoutException at line 100\n```");
        assertThat(prompt).contains("### 서비스 'order-service'의 최근 3분간 에러 로그 ###");
        assertThat(prompt).contains("```\nDatabaseConnectionException\n```");
        assertThat(prompt).endsWith("\n이 로그들의 핵심 원인은 무엇이며, 어떤 해결 방안을 제안할 수 있나요?");
    }
}
