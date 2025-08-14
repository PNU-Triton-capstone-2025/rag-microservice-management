package com.triton.msa.triton_dashboard.private_data.service;

import com.triton.msa.triton_dashboard.private_data.ExtractedFile;
import com.triton.msa.triton_dashboard.private_data.dto.UploadedFileResultDto;
import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataResponseDto;
import com.triton.msa.triton_dashboard.private_data.dto.PrivateDataUploadResultDto;
import com.triton.msa.triton_dashboard.private_data.exception.UnsupportedFileTypeException;
import com.triton.msa.triton_dashboard.private_data.util.FileTypeUtil;
import com.triton.msa.triton_dashboard.private_data.util.ZipExtractor;
import com.triton.msa.triton_dashboard.private_data.repository.PrivateDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateDataService {

    private final PrivateDataPersistenceService privateDataPersistenceService;
    private final PrivateDataRepository privateDataRepository;
    private final ZipExtractor zipExtractor;

    public PrivateDataUploadResultDto unzipAndSaveFiles(Long projectId, MultipartFile file) {
        // 유효한 zip 파일인지 검증
        validateZipFile(file);

        List<UploadedFileResultDto> saved = new ArrayList<>();
        List<UploadedFileResultDto> skipped = new ArrayList<>();

        // zip 파일 압축 해제 (실패 시 PrivateDataUnzipException 발생)
        List<ExtractedFile> extractedFiles = zipExtractor.extract(file, skipped);

        // 압축 해제된 파일들 저장 후, 결과 리턴
        return saveUnzippedFiles(projectId, extractedFiles, skipped, saved);
    }

    private void validateZipFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (file.isEmpty()) {
            throw new UnsupportedFileTypeException("파일이 비어있습니다.");
        }
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new UnsupportedFileTypeException("지원되지 않는 파일 형식입니다. .zip 파일만 업로드해주세요.");
        }
    }

    private PrivateDataUploadResultDto saveUnzippedFiles(Long projectId, List<ExtractedFile> extractedFiles, List<UploadedFileResultDto> skipped, List<UploadedFileResultDto> saved) {
        for (ExtractedFile doc : extractedFiles) {
            if (FileTypeUtil.isAllowed(doc.filename())) {
                // 압축 해제된 파일(지원되는 확장자에 한해서)에 대해 저장시도
                boolean isSuccess = privateDataPersistenceService.saveFile(projectId, doc, skipped);
                if (isSuccess) saved.add(new UploadedFileResultDto(doc.filename(), "저장 성공"));
                // 저장 실패된 파일 목록은 saveFile 메서드에서 처리
            } else {
                skipped.add(new UploadedFileResultDto(doc.filename(), "허용되지 않음"));
            }
        }

        int s = saved.size(), k = skipped.size();
        return new PrivateDataUploadResultDto(
                "업로드 완료 (성공 %d건, 스킵 %d건)".formatted(s, k), saved, skipped
        );
    }

    @Transactional(readOnly = true)
    public List<PrivateDataResponseDto> getPrivateDataList(Long projectId) {
        return privateDataRepository.getPrivateDataDtosByProjectId(projectId)
                .stream()
                .map(PrivateDataResponseDto::from)
                .toList();
    }
}