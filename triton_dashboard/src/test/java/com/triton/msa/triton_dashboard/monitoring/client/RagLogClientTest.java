package com.triton.msa.triton_dashboard.monitoring.client;

import com.triton.msa.triton_dashboard.monitoring.dto.ErrorAnalysisRequestDto;
import com.triton.msa.triton_dashboard.monitoring.dto.RagLogResponseDto;
import com.triton.msa.triton_dashboard.monitoring.dto.ResourceAnalysisRequestDto;
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
        setField(ragLogClient, "ragServerBaseUrl", baseUrl);
    }

    @Test
    @DisplayName("RAG 서버 에러 로그 분석 요청 - 200")
    void analyzeErrorLogs() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"title\":\"Error Analysis Result\",\"answer\":\"Everything is fine\"}")
                .addHeader("Content-Type", "application/json"));
        ErrorAnalysisRequestDto requestDto = new ErrorAnalysisRequestDto("project-1", "openai", "some-key", "Analyze this log.", "yamls");

        // when
        Mono<RagLogResponseDto> result = ragLogClient.analyzeErrorLogs(requestDto);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.title().equals("Error Analysis Result")
                                && response.answer().equals("Everything is fine"))
                .verifyComplete();
    }

    @Test
    @DisplayName("RAG 서버 리소스 분석 요청 - 200")
    void analyzeResourceSettings() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"title\":\"Resource Analysis Result\",\"answer\":\"Resources are fine\"}")
                .addHeader("Content-Type", "application/json"));
        ResourceAnalysisRequestDto requestDto = new ResourceAnalysisRequestDto("project-1", "openai", "gpt-4", "some-key", "resource usage", "yamls");

        // when
        Mono<RagLogResponseDto> result = ragLogClient.analyzeResourceSettings(requestDto);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.title().equals("Resource Analysis Result")
                                && response.answer().equals("Resources are fine"))
                .verifyComplete();
    }


    @Test
    @DisplayName("RAG 서버 응답이 500 에러일 때 Mono.empty() 반환")
    void analyzeLogsServerError() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        ErrorAnalysisRequestDto requestDto = new ErrorAnalysisRequestDto("project-1", "openai", "some-key", "Analyze this log.", "yamls");


        // when
        Mono<RagLogResponseDto> result = ragLogClient.analyzeErrorLogs(requestDto);

        // then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
