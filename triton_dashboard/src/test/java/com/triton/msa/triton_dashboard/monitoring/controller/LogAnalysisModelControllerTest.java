package com.triton.msa.triton_dashboard.monitoring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triton.msa.triton_dashboard.common.jwt.JwtTokenProvider;
import com.triton.msa.triton_dashboard.monitoring.entity.LogAnalysisModel;
import com.triton.msa.triton_dashboard.monitoring.service.LogAnalysisModelService;
import com.triton.msa.triton_dashboard.user.entity.LlmModel;
import com.triton.msa.triton_dashboard.user.entity.LlmProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogAnalysisModelController.class)
public class LogAnalysisModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LogAnalysisModelService modelService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser
    @DisplayName("특정 프로젝트의 로그 분석 엔드포인트 조회 - 200")
    void getEndpoint() throws Exception {
        Long projectId = 10000L;
        LogAnalysisModel model = new LogAnalysisModel(LlmProvider.OPENAI, LlmModel.GPT_4O);
        given(modelService.getAnalysisModel(projectId)).willReturn(model);

        mockMvc.perform(get("/api/projects/{projectId}/monitoring/endpoint", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value(LlmProvider.OPENAI.toString().toLowerCase()))
                .andExpect(jsonPath("$.model").value(LlmModel.GPT_4O.toString()));

        verify(modelService).getAnalysisModel(projectId);
    }


}
