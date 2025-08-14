package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadedFileResultDto;
import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataResponseDto;
import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataUploadResultDto;
import com.triton.msa.triton_dashboard.private_data.util.FileTypeUtil;
import com.triton.msa.triton_dashboard.private_data.util.ZipExtractor;
import com.triton.msa.triton_dashboard.private_data.repository.PrivateDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateDataService {

    private final PrivateDataPersistenceService privateDataPersistenceService;
    private final PrivateDataRepository privateDataRepository;
    private final ZipExtractor zipExtractor;

    public PrivateDataUploadResultDto unzipAndSaveFiles(Long projectId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            return new PrivateDataUploadResultDto("지원되지 않는 파일 형식입니다. .zip 파일만 업로드해주세요.", List.of(), List.of());
        }

        List<UploadedFileResultDto> saved = new ArrayList<>();
        List<UploadedFileResultDto> skipped = new ArrayList<>();

        try {
            List<ExtractedFile> extractedFiles = zipExtractor.extract(file, skipped);

            for (ExtractedFile doc : extractedFiles) {
                if (FileTypeUtil.isAllowed(doc.filename())) {
                    boolean isSuccess = privateDataPersistenceService.saveFile(projectId, doc, skipped);
                    if (isSuccess) saved.add(new UploadedFileResultDto(doc.filename(), "저장 성공"));
                } else {
                    skipped.add(new UploadedFileResultDto(doc.filename(), "허용되지 않음"));
                }
            }

            return new PrivateDataUploadResultDto("업로드 완료", saved, skipped);

        } catch (IOException e) {
            return new PrivateDataUploadResultDto("압축 해제 실패: " + e.getMessage(), List.of(), List.of());
        }
    }

    @Transactional(readOnly = true)
    public List<PrivateDataResponseDto> getPrivateDataList(Long projectId) {
        return privateDataRepository.getPrivateDataDtosByProjectId(projectId)
                .stream()
                .map(PrivateDataResponseDto::from)
                .toList();
    }
}