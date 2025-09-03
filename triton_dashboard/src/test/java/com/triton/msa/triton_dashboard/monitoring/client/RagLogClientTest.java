package com.triton.msa.triton_dashboard.monitoring.client;

import com.triton.msa.triton_dashboard.monitoring.dto.RagLogRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.springframework.test.util.ReflectionTestUtils.setField;

public class RagLogClientTest {

    private static MockWebServer mockWebServer;
    private RagLogClient ragLogClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.create(baseUrl);
        ragLogClient = new RagLogClient(webClient);
        setField(ragLogClient, "ragServerUrl", baseUrl);
    }

    @Test
    @DisplayName("RAG 서버 로그 분석 요청 - 200")
    void analyzeLogs() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"title\":\"Analysis Result\",\"llm_response\":\"Everything is fine\"}")
                .addHeader("Content-Type", "application/json"));
        RagLogRequestDto requestDto = new RagLogRequestDto("project-1", "openai", "GPT_4", "Analyze this log.");

        // when
        Mono<RagLogResponseDto> result = ragLogClient.analyzeLogs(1L, requestDto);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                    response.title().equals("Analysis Result")
                            && response.answer().equals("Everything is fine"))
                .verifyComplete();
    }

    @Test
    @DisplayName("RAG 서버 응답이 500 에러일 때 Mono.empty() 반환")
    void analyzeLogsServerError() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        RagLogRequestDto requestDto = new RagLogRequestDto("project-1", "openai", "GPT_4", "Analyze this log.");

        // when
        Mono<RagLogResponseDto> result = ragLogClient.analyzeLogs(1L, requestDto);

        // then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
