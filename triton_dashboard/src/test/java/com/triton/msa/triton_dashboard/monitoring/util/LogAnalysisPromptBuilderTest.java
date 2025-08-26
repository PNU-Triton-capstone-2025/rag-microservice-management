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
        String prompt = LogAnalysisPromptBuilder.buildPrompt(errorLogs, null);

        // then
        assertThat(prompt).contains("### 서비스 'auth-service'의 에러 로그 ###");
        assertThat(prompt).contains("```\nNullPointerException at line 50\nTimeoutException at line 100\n```");
        assertThat(prompt).contains("### 서비스 'order-service'의 에러 로그 ###");
        assertThat(prompt).contains("```\nDatabaseConnectionException\n```");
    }
}
