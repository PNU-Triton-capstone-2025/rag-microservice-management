package com.triton.msa.triton_dashboard.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorAnalysisRequestDto(
        @JsonProperty("es_index")
        String esIndex,
        String model,
        @JsonProperty("api_key")
        String apiKey,
        @JsonProperty("err_log")
        String errLog,
        String yamls
) {
}
