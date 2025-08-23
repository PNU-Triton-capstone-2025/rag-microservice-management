package com.triton.msa.triton_dashboard.log_deployer.service;

import com.triton.msa.triton_dashboard.common.jwt.JwtTokenProvider;
import com.triton.msa.triton_dashboard.log_deployer.controller.DeployController;
import com.triton.msa.triton_dashboard.log_deployer.dto.LogDeployerCustomDto;
import org.apache.tika.metadata.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeployController.class)
class DeployControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogDeployerService logDeployerService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser
    @DisplayName("로그 수집기 배포 파일 다운로드")
    void downloadConfig() throws Exception {
        Long projectId = 10000L;
        byte[] dummyZipBytes = "dummy-zip-content".getBytes();


        given(logDeployerService.generateDeploymentZip(new LogDeployerCustomDto(projectId, "logging", 5044))).willReturn(dummyZipBytes);

        ResultActions resultActions = mockMvc.perform(get("/projects/{projectId}/deploy/download-config", projectId));

        resultActions.andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"log-deploy-config-" + projectId + ".zip\""))
                .andExpect(content().bytes(dummyZipBytes));
    }
}