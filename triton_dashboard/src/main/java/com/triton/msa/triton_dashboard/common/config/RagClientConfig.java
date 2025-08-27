package com.triton.msa.triton_dashboard.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RagClientConfig {

    @Bean(name = "ragWebClient")
    public WebClient ragWebClient(WebClient.Builder builder,
                                  @Value("${rag.service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
