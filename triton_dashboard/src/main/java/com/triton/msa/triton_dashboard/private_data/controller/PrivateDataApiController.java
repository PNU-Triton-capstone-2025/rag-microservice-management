package com.triton.msa.triton_dashboard.private_data.controller;

import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataResponseDto;
import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.private_data.service.PrivateDataPersistenceService;
import com.triton.msa.triton_dashboard.private_data.service.PrivateDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/private-data")
@RequiredArgsConstructor
public class PrivateDataApiController {
    private final PrivateDataService privateDataService;
    private final PrivateDataPersistenceService privateDataPersistenceService;

    @GetMapping
    public ResponseEntity<List<PrivateDataResponseDto>> listPrivateData(@PathVariable Long projectId) {
        List<PrivateDataResponseDto> privateDataList = privateDataService.getPrivateDataList(projectId);

        return ResponseEntity.ok(privateDataList);
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResultDto> uploadZip(
            @PathVariable("projectId") Long projectId,
            @RequestParam("file")MultipartFile file
    ) {
        UploadResultDto resultDto = privateDataService.unzipAndSaveFiles(projectId, file);
        return ResponseEntity.ok(resultDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrivateData(@PathVariable Long projectId, @PathVariable Long id) {
        privateDataPersistenceService.deletePrivateData(projectId, id);
        return ResponseEntity.noContent().build();
    }
}
