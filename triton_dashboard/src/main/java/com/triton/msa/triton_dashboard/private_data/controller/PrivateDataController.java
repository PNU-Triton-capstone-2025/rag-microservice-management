package com.triton.msa.triton_dashboard.private_data.controller;

import com.triton.msa.triton_dashboard.private_data.dto.UploadResultDto;
import com.triton.msa.triton_dashboard.private_data.service.PrivateDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/private-data")
@RequiredArgsConstructor
public class PrivateDataController {

    private final PrivateDataService privateDataService;

    @PostMapping("/upload")
    public String uploadZip(
            @PathVariable("projectId") Long projectId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        UploadResultDto result;

        try {
            result = privateDataService.unzipAndSaveFiles(projectId, file);
        } catch (MaxUploadSizeExceededException e) {
            result = new UploadResultDto("업로드 용량 초과: 10MB 이하의 zip 파일만 업로드 가능합니다.", List.of(), List.of());
        } catch (IllegalArgumentException e) {
            result = new UploadResultDto(e.getMessage(), List.of(), List.of());
        }

        redirectAttributes.addFlashAttribute("uploadResult", result);
        return "redirect:/projects/" + projectId + "/private-data";
    }
}
