package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.private_data.util.ZipExtractor;
import com.triton.msa.triton_dashboard.private_data.entity.PrivateData;
import com.triton.msa.triton_dashboard.project.entity.Project;
import com.triton.msa.triton_dashboard.private_data.repository.PrivateDataRepository;
import com.triton.msa.triton_dashboard.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateDataServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private PrivateDataRepository privateDataRepository;

    @Mock
    private ZipExtractor zipExtractor;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PrivateDataService privateDataService;

    @Test
    void unzipAndSaveFiles() throws IOException {
        // given
        Long projectId = 1L;
        MultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", new byte[10]);

        ExtractedFile extractedFile = new ExtractedFile("doc.txt", "file content", Instant.now());
        List<ExtractedFile> extractedFiles = List.of(extractedFile);

        Map<String, Object> esResponse = Map.of("_id", "123");
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(esResponse);

        when(zipExtractor.extract(any(), any())).thenReturn(extractedFiles);
        when(projectService.getProject(projectId)).thenReturn(new Project());

        // when
        UploadResultDto result = privateDataService.unzipAndSaveFiles(projectId, file);

        // then
        assertEquals("업로드 완료", result.message());
        assertTrue(result.savedFilenames().contains("doc.txt"));
        assertTrue(result.skippedFilenames().isEmpty());
    }


    @Test
    void deletePrivateData() {
        // given
        Long projectId = 1L;
        Long dataId = 100L;
        String esId = "abc123";

        PrivateData mockData = new PrivateData();
        mockData.setEsId(esId);
        mockData.setId(dataId);

        when(privateDataRepository.findByIdAndProjectId(dataId, projectId))
                .thenReturn(java.util.Optional.of(mockData));

        // when
        privateDataService.deletePrivateData(projectId, dataId);

        // then
        // 실제 데이터 있어야 통과해서 테스트 코드는 통과 안하지만, 실제로 삭제되는거 확인함.
        verify(restTemplate).delete("http://localhost:30920/project-" + projectId + "/_doc/" + esId);

        verify(privateDataRepository).deleteById(dataId);
    }

    @Test
    void getPrivateDataList() {
        // given
        Long projectId = 1L;
        List<PrivateData> mockList = List.of(new PrivateData(), new PrivateData());

        when(privateDataRepository.findByProjectId(projectId)).thenReturn(mockList);

        // when
        List<PrivateData> result = privateDataService.getPrivateDataList(projectId);

        // then
        assertEquals(2, result.size());
        verify(privateDataRepository).findByProjectId(projectId);
    }
}